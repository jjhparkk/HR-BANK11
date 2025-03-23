package com.team11.hrbank.module.common.dto;

import java.util.Base64;
import java.util.List;

public record CursorPageResponse<T>(
    List<T> content,
    String nextCursor,
    Long nextIdAfter,
    int size,
    long totalElements,
    boolean hasNext
) {

  /**
   * 커서 기반 페이지 응답 객체 생성 (기존 메소드 유지)
   * @param content       페이지 컨텐츠
   * @param lastId        마지막 요소의 ID
   * @param size          페이지 크기
   * @param totalElements 총 요소 수
   * @return 커서 페이지 응답
   */
  public static <T> CursorPageResponse<T> of(List<T> content, Long lastId, int size, long totalElements) {
    boolean hasNext =
        !content.isEmpty() && content.size() == size && totalElements > content.size();
    String nextCursor = hasNext ?
        java.util.Base64.getEncoder().encodeToString(("{\"id\":" + lastId + "}").getBytes()) :
        null;

    return new CursorPageResponse<>(
        content,
        nextCursor,
        hasNext ? lastId : null,
        size,
        totalElements,
        hasNext
    );
  }

  /**
   * 커서 기반 페이지 응답 객체 생성 (changelog, backup 등 다른 모듈용)
   * @param content       페이지 컨텐츠
   * @param cursorValue   다음 페이지 커서 값
   * @param lastId        마지막 요소의 ID
   * @param size          페이지 크기
   * @param totalElements 총 요소 수
   * @param hasNext       다음 페이지 존재 여부
   * @return 커서 페이지 응답
   */
  public static <T> CursorPageResponse<T> of(
      List<T> content,
      String cursorValue,
      Long lastId,
      int size,
      long totalElements,
      boolean hasNext) {

    int effectiveSize = hasNext ? size : content.size();

    return new CursorPageResponse<>(
        content,
        hasNext ? cursorValue : null,
        hasNext ? lastId : null,
        effectiveSize,
        totalElements,
        hasNext
    );
  }

  /**
   * null 커서 버전 (List<Object>와 함께 사용)
   */
  public static <T> CursorPageResponse<T> of(
      List<T> content,
      Object nullCursor1,
      Object nullCursor2,
      int size,
      long totalElements,
      boolean hasNext) {

    return new CursorPageResponse<>(
        content,
        null,
        null,
        size,
        totalElements,
        hasNext
    );
  }

  /**
   * 커서 문자열에서 ID 추출
   * @param cursor 커서 문자열
   * @return 추출된 ID
   */
  public static Long extractIdFromCursor(String cursor) {
    if (cursor == null || cursor.isEmpty()) {
      return null;
    }

    try {
      // Base64 인코딩된 ID일 경우 디코딩 시도
      String decoded = new String(Base64.getDecoder().decode(cursor));
      return Long.parseLong(decoded.replace("{\"id\":", "").replace("}", ""));
    } catch (Exception e) {
      // 디코딩에 실패한 경우, null 반환
      return null;
    }
  }
}