package com.team3.dto.response;

/**
 * API 응답을 나타내는 DTO 클래스
 * <p>
 * HMS 서버로부터 받은 HTTP 응답을 캡슐화하여 클라이언트에게 제공한다.
 * 응답 상태 코드, 본문, 성공 여부를 포함하며, 모든 API 호출의 표준 응답 형식으로 사용된다.
 * </p>
 * 
 * @author bang9634
 * @since 2025-11-22
 */
public class ApiResponse {
    // HTTP 응답 상태 코드
    private final int statusCode;

    // 응답 본문
    private final String body;

    // 요청 성공 여부
    private final boolean success;
    
    /**
     * ApiResponse 생성자
     * <p>
     * HTTP 응답 정보를 담은 불변 객체를 생성한다.
     * 주로 API 클래스 내부에서 서버 응답을 변환할 때 사용된다.
     * 응답 성공 여부는 statusCode가 200 ~ 299인 경우이며,
     * 나머지의 경우는 모두 false를 저장한다.
     * </p>
     * 
     * @param statusCode HTTP 응답 상태 코드
     * @param body 응답 본문 (성공 메시지, 데이터, 에러 메시지 등)
     */
    public ApiResponse(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body;
        this.success = (statusCode >= 200 && statusCode < 300);
    }

    /**
     * 에러 응답 객체를 생성하는 정적 팩토리 메서드
     * <p>
     * 네트워크 오류나 예외 상황에서 사용하기 위한 편의 메서드다.
     * statusCode는 0으로, success는 false로 자동 설정된다.
     * </p>
     * 
     * <h4>사용 예시 (API 클래스 내부):</h4>
     * <pre>{@code
     * try {
     *     HttpResponse<String> response = sendPost("/api/users/login", request);
     *     return new ApiResponse(response.statusCode(), response.body(), true);
     *     
     * } catch (IOException e) {
     *     // 네트워크 오류 시 에러 응답 생성
     *     return ApiResponse.error("서버 연결 실패: " + e.getMessage());
     * }
     * }</pre>
     * 
     * @param message 에러 메시지
     * @return 에러 응답 객체
     * 
     * @apiNote 이 메서드로 생성된 응답은 HTTP 레벨이 아닌
     *          클라이언트 측 오류(네트워크, 타임아웃 등)를 나타낸다.
     */
    public static ApiResponse error(String message) {
        return new ApiResponse(0, message);
    }
    
    public int getStatusCode() { return statusCode; }
    public String getBody() { return body; }
    public boolean isSuccess() { return success; }
}