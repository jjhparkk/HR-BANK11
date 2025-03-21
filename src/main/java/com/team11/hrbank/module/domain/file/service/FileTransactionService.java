package com.team11.hrbank.module.domain.file.service;

import com.team11.hrbank.module.common.exception.ResourceNotFoundException;
import com.team11.hrbank.module.domain.file.File;
import com.team11.hrbank.module.domain.file.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FileTransactionService {

  private final FileRepository fileRepository;

  /**
   * 파일 메타데이터 저장
   */
  public File saveFileMetadata(String originalName, String format, String filePath, long size) {
    File fileEntity = new File();
    fileEntity.setFileName(originalName);
    fileEntity.setFormat(format);
    fileEntity.setFilePath(filePath);
    fileEntity.setSize(size);

    File savedFile = fileRepository.save(fileEntity);
    log.info("파일 메타데이터 저장 성공: ID={}, 파일명={}", savedFile.getId(), savedFile.getFileName());
    return savedFile;
  }

  /**
   * 파일 엔티티 저장
   */
  public File saveFile(File fileEntity) {
    if (fileEntity == null) {
      throw new IllegalArgumentException("파일 엔티티가 null입니다.");
    }
    return fileRepository.save(fileEntity);
  }

  /**
   * ID로 파일 조회
   */
  @Transactional(readOnly = true)
  public File getFileById(Long fileId) {
    if (fileId == null) {
      throw new IllegalArgumentException("파일 ID가 null입니다.");
    }
    return fileRepository.findById(fileId)
        .orElseThrow(() -> ResourceNotFoundException.of("File", "fileId", fileId));
  }

  /**
   * 파일 엔티티 삭제 (DB에서만)
   */
  public void deleteFileEntity(File fileEntity) {
    if (fileEntity == null) {
      throw new IllegalArgumentException("삭제 시도한 파일 엔티티가 null 입니다");
    }

    fileRepository.delete(fileEntity);
    log.info("파일 메타데이터 삭제 성공: ID={}", fileEntity.getId());
  }
}