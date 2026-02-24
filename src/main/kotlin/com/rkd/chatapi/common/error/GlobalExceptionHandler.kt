package com.rkd.chatapi.common.error

import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.rkd.chatapi.common.error.ErrorResponse.FieldError
import com.rkd.chatapi.common.error.exception.BusinessException
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.validation.BindingResult
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.resource.NoResourceFoundException

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(ex: BusinessException): ResponseEntity<ErrorResponse> {
        log.error(ex.message, ex)
        val errorCode = ex.errorCode
        return ResponseEntity.status(errorCode.httpStatus)
            .body(ErrorResponse.of(errorCode))
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(ex: AccessDeniedException): ResponseEntity<ErrorResponse> {
        log.error(ex.message, ex)
        val errorCode = ErrorCode.ACCESS_DENIED
        return ResponseEntity.status(errorCode.httpStatus)
            .body(ErrorResponse.of(errorCode))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        log.error(ex.message, ex)
        val errorCode = ErrorCode.INPUT_INVALID_VALUE
        val errors = fieldErrors(ex.bindingResult)
        return ResponseEntity.status(errorCode.httpStatus)
            .body(ErrorResponse.of(errorCode, errors))
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(ex: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> {
        log.error(ex.message, ex)
        val errorCode = ErrorCode.INPUT_INVALID_VALUE
        val errors = when (val cause = ex.cause) {
            is InvalidFormatException -> listOf(fieldTypeError(cause))
            is MismatchedInputException -> listOf(fieldNullError(cause))
            else -> listOf()
        }
        return ResponseEntity.status(errorCode.httpStatus)
            .body(ErrorResponse.of(errorCode, errors))
    }

    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResourceFoundException(ex: NoResourceFoundException): ResponseEntity<ErrorResponse> {
        log.error(ex.message, ex)
        return ResponseEntity.notFound().build()
    }

    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<ErrorResponse> {
        log.error(ex.message, ex)
        val errorCode = ErrorCode.INTERNAL_SERVER_ERROR
        return ResponseEntity.status(errorCode.httpStatus)
            .body(ErrorResponse.of(errorCode))
    }

    private fun fieldErrors(bindingResult: BindingResult): List<FieldError> {
        return bindingResult.fieldErrors.map { fieldError ->
            FieldError(
                field = fieldError.field,
                value = fieldError.rejectedValue,
                reason = fieldError.defaultMessage ?: "Invalid value"
            )
        }
    }

    private fun fieldTypeError(ex: InvalidFormatException): FieldError {
        return FieldError(
            field = pathToField(ex.path),
            value = ex.value,
            reason = "Type mismatch"
        )
    }

    private fun fieldNullError(ex: MismatchedInputException): FieldError {
        return FieldError(
            field = pathToField(ex.path),
            value = null,
            reason = "Required value is missing"
        )
    }

    private fun pathToField(path: List<com.fasterxml.jackson.databind.JsonMappingException.Reference>): String {
        return path.joinToString(".") { ref ->
            when {
                ref.fieldName != null -> ref.fieldName
                ref.index >= 0 -> "[${ref.index}]"
                else -> ""
            }
        }.ifBlank { "" }
    }
}
