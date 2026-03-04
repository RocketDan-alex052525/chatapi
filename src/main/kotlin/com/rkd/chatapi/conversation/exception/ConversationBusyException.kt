package com.rkd.chatapi.conversation.exception

import com.rkd.chatapi.common.error.ErrorCode
import com.rkd.chatapi.common.error.exception.BusinessException

class ConversationBusyException : BusinessException(ErrorCode.CONVERSATION_BUSY)
