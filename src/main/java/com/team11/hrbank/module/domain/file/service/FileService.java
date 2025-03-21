package com.team11.hrbank.module.domain.file.service;

import com.team11.hrbank.module.common.config.FileStorageProperties;
import com.team11.hrbank.module.domain.file.File;
import com.team11.hrbank.module.domain.file.exception.FileDeleteException;
import com.team11.hrbank.module.domain.file.exception.FileDownloadException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {
  private final FileStorageProperties fileStorageProperties;
  private final FileTransactionService fileTransactionService;

  /**
   * 파일 업로드 처리
   * @param file 업로드할 파일
   * @return 저장된 파일 엔티티
   * @throws IOException 파일 저장 중 발생한 예외
   */
  public File uploadFile(MultipartFile file) throws IOException {
    // 파일 유효성 검증
    validateFile(file);

    // 실제 파일 저장
    String filePath = saveActualFile(file);

    try {
      // DB에 파일 메타데이터 저장 (트랜잭션)
      return fileTransactionService.saveFileMetadata(
          file.getOriginalFilename(),
          FilenameUtils.getExtension(file.getOriginalFilename()),
          filePath,
          file.getSize()
      );
    } catch (Exception e) {
      // DB 저장 실패 시 실제 파일 삭제
      log.error("파일 메타데이터 저장 실패 - 파일시스템 실제 파일 삭제 시작, 경로: {}", filePath, e);
      deleteActualFile(filePath);
      throw e;
    }
  }

  /**
   * 파일 업데이트 처리
   * 새 파일 업로드 후 이전 파일 삭제
   */
  @Transactional
  public File updateFile(File oldFile, MultipartFile newFileData) throws IOException {
    if (newFileData == null || newFileData.isEmpty()) {
      log.error("업로드할 새 파일이 null이거나 비어있습니다.");
      throw new IllegalArgumentException("업로드할 새 파일이 비어있습니다.");
    }

    log.info("파일 업데이트 시작: oldFile={}, newFile={}",
        (oldFile != null) ? oldFile.getId() + " - " + oldFile.getFileName() : "없음",
        newFileData.getOriginalFilename());

    // 1. 새 파일 업로드
    File newFile = uploadFile(newFileData);
    log.info("새 파일 업로드 성공: ID={}, 경로={}", newFile.getId(), newFile.getFilePath());

    // 2. 이전 파일 삭제
    if (oldFile != null) {
      try {
        deleteFile(oldFile);
        log.info("이전 파일 삭제 성공: ID={}", oldFile.getId());
      } catch (Exception e) {
        log.warn("이전 파일 삭제 실패: id={}, exception={}", oldFile.getId(), e.getMessage());
        // 이전 파일 삭제 실패 처리 - 일단은 무시
      }
    }

    return newFile;
  }

  /**
   * 파일 다운로드
   * @param fileId 파일 ID
   * @return 파일 바이트 배열
   */
  public byte[] downloadFile(Long fileId) {
    try {
      File fileEntity = fileTransactionService.getFileById(fileId);
      Path filePath = Paths.get(fileEntity.getFilePath());

      if (!Files.exists(filePath)) {
        log.warn("해당 파일이 물리적으로 존재하지 않습니다 {}", filePath);
        return new byte[0]; //빈바이트 배열 반환 (대안: 프론트 수정해서 기본 이미지 반환)
//        throw ResourceNotFoundException.of("File", "filePath", fileEntity.getFilePath());
      }

      return Files.readAllBytes(filePath);
    } catch (IOException e) {
      log.error("파일 읽기 실패: {}", e.getMessage());
      throw new FileDownloadException("파일을 읽을 수 없습니다: " + fileId, e);
    }
  }

  /**
   * 파일 삭제 (DB + 물리 파일)
   * @param fileEntity 삭제할 파일 엔티티
   */
  public void deleteFile(File fileEntity) {
    if (fileEntity == null) {
      throw new IllegalArgumentException("삭제 시도한 파일 엔티티가 null 입니다");
    }

    String filePath = fileEntity.getFilePath();
    Long fileId = fileEntity.getId();

    try {
      // 1. DB에서 삭제
      fileTransactionService.deleteFileEntity(fileEntity);

      // 2. 실제 파일 삭제
      try {
        deleteActualFile(filePath);
      } catch (FileDeleteException e) {
        log.error("DB에서는 삭제되었지만 실제 파일은 삭제 실패: {}", filePath, e);
        // 예외를 던지지 않고 로그만 남김
      }
    } catch (Exception e) {
      log.error("파일 메타데이터 삭제 실패: {}", e.getMessage(), e);
      throw new FileDeleteException("파일 메타데이터 삭제 실패: ID=" + fileId, e);
    }
  }

  /**
   * 실제 파일 삭제
   * @param filePath 파일 경로
   */
  public void deleteActualFile(String filePath) {
    if (filePath == null || filePath.trim().isEmpty()) {
      throw new IllegalArgumentException("삭제할 파일 경로가 비어 있습니다.");
    }

    try {
      Path path = Paths.get(filePath);
      if (Files.exists(path)) {
        Files.delete(path);
        log.info("파일 삭제 성공: {}", filePath);
      }
    } catch (IOException e) {
      log.error("파일 삭제 실패: {}", e.getMessage(), e);
      throw new FileDeleteException("파일 삭제 중 오류가 발생했습니다: " + filePath, e);
    }
  }

  /**
   * 파일 업로드 유효성 검증
   * @param file 업로드할 파일
   */
  private void validateFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("업로드할 파일이 없습니다.");
    }

    String originalName = file.getOriginalFilename();
    if (originalName == null || originalName.trim().isEmpty()) {
      throw new IllegalArgumentException("파일 이름이 없습니다.");
    }

    log.info("파일 업로드 검증 완료: 파일명={}, 크기={}bytes", originalName, file.getSize());
  }

  /**
   * 파일 시스템에 실제 파일 저장 (트랜잭션 외부)
   * @param file 업로드할 파일
   * @return 저장된 파일 경로
   * @throws IOException 파일 저장 중 발생한 예외
   */
  private String saveActualFile(MultipartFile file) throws IOException {
    String originalName = file.getOriginalFilename();
    log.info("물리적 파일 저장 시작: 파일명={}, 크기={}bytes", originalName, file.getSize());

    // 저장 디렉토리 확인 및 생성
    Path rootPath = Paths.get(fileStorageProperties.getProfileImages());
    checkDirectoryExists(rootPath);

    // 고유 파일명 생성
    String timestamp = String.valueOf(System.currentTimeMillis());
    String uniqueFileName = timestamp + "_" + UUID.randomUUID() + "_" + originalName;
    Path filePath = rootPath.resolve(uniqueFileName);

    // 파일 복사
    try {
      Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
      log.info("물리적 파일 저장 성공: {}", filePath);
      return filePath.toString();
    } catch (IOException e) {
      log.error("물리적 파일 저장 실패: {}", e.getMessage(), e);
      throw new IOException("파일을 저장할 수 없습니다: " + e.getMessage(), e);
    }
  }

  /**
   * 디렉토리 존재 확인 및 생성
   * @param rootPath 확인할 디렉토리 경로
   * @throws IOException 디렉토리 생성 중 발생한 예외
   */
  private static void checkDirectoryExists(Path rootPath) throws IOException {
    if (!Files.exists(rootPath)) {
      try {
        Files.createDirectories(rootPath);
        log.info("디렉토리 생성 완료: {}", rootPath);
      } catch (IOException e) {
        log.error("저장 디렉토리 생성 실패: {}", e.getMessage());
        throw new IOException("파일 저장 디렉토리를 생성할 수 없습니다: " + e.getMessage());
      }
    }
  }

  /**
   * ID로 파일 조회
   * @param fileId 파일 ID
   * @return 조회된 파일 엔티티
   */
  public File getFileById(Long fileId) {
    return fileTransactionService.getFileById(fileId);
  }

  /**
   * 파일 삭제 (DB + 물리 파일)
   * @param fileId 삭제할 파일 ID
   */
  @Transactional
  public void deleteFile(Long fileId) {
    File fileEntity = fileTransactionService.getFileById(fileId);
    deleteFile(fileEntity);
  }


  /**
   * 파일 엔티티 저장 (filetranactionservice에 위임)
   * @param fileEntity
   * @return
   */
  public File saveFile(File fileEntity) {
    return fileTransactionService.saveFile(fileEntity);
  }
}