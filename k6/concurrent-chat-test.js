import http from 'k6/http';
import { check } from 'k6';

export const options = {
    vus: 5,        // 동시 사용자 5명
    iterations: 5, // 총 5번 (각 VU가 1번씩)
};

const COOKIE = 'ACCESS_TOKEN';
const CONVERSATION_ID = conversatio_id;

export default function () {
    const res = http.post(
        'http://localhost:8080/api/chat/completions/stream',
        JSON.stringify({
            conversationId: CONVERSATION_ID,
            content: '테스트 메시지입니다.',
        }),
        {
            headers: {
                'Content-Type': 'application/json',
                'Cookie': `ACCESS_TOKEN_COOKIE=${COOKIE}`,
            },
        }
    );

    check(res, {
        'status 200': (r) => r.status === 200,
    });

    console.log(`VU ${__VU} | status: ${res.status}`);
}
