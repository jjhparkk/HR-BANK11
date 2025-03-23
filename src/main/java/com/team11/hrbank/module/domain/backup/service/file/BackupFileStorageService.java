package com.team11.hrbank.module.domain.backup.service.file;

import com.team11.hrbank.module.common.config.FileStorageProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

/**
 * 백업 데이터를 CSV 파일로 저장하는 서비스 (OOM 방지 적용)
 */
@Slf4j
@Service
public class BackupFileStorageService {
    private final Path backupDir;
    private final Path errorLogDir;
    private static DateTimeFormatter FILE_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").withZone(ZoneId.systemDefault());

    public BackupFileStorageService(FileStorageProperties properties){
        this.backupDir = createDirectoryIfNotExists(Paths.get(properties.getBackupFiles()));
        this.errorLogDir = createDirectoryIfNotExists(Paths.get(properties.getErrorLogs()));
    }

    private Path createDirectoryIfNotExists(Path directory) {
        try {
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
                log.info("디렉토리 생성 완료: {}", directory);
            }
            return directory;
        } catch (IOException e) {
            log.info("디렉토리 생성 실패: {}", directory, e);
            throw new RuntimeException("디렉토리 생성 실패: " + directory, e);
        }
    }


    /**
     * 백업 데이터 csv 파일로 저장
     * @param backupDataStream 각행의 데이터가 csv 형식으로 포멧된 문자열 스트림
     * @return 저장된 파일 경로
     */
    public String saveBackupToCsv(Stream<String> backupDataStream) throws IOException {
        String filename = "backup_" + FILE_TIMESTAMP_FORMAT.format(Instant.now()) + ".csv";
        Path filePath = backupDir.resolve(filename);

        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8);
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT)){

            writer.write("\uFEFF"); //BOM 추가 (excel 한글 인코딩 인식)


            try {
                backupDataStream.forEach(line -> {
                    try {
                        writer.write(line);
                        writer.newLine();
                    } catch (IOException e) {
                        throw new UncheckedIOException("csv 쓰기 실패", e);
                    }
                });
            } catch (UncheckedIOException e) {
                throw e.getCause();//원래 ioexception로 재변환뒤 위로 던짐
            }
        }
        log.info("백업 파일 저장 완료: {}", filePath);
        return filePath.toString();
    }

    /**
     * 직원 데이터 CSV 생성 - 헤더와 데이터 분리 버전
     * @param headers CSV 헤더
     * @param dataStream 데이터 스트림
     * @return 저장된 파일 경로
     */
    public String createEmployeeBackupCsv(String[] headers, Stream<String[]> dataStream) throws IOException {
        String filename = "employees_backup_" + FILE_TIMESTAMP_FORMAT.format(Instant.now()) + ".csv";
        Path filePath = backupDir.resolve(filename);

        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8);
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT)) {

            writer.write("\uFEFF");

            // 헤더 작성
            csvPrinter.printRecord((Object[]) headers);

            // 데이터 스트림 작성
            dataStream.forEach(record -> {
                try {
                    csvPrinter.printRecord((Object[]) record);
                } catch (IOException e) {
                    throw new UncheckedIOException("CSV 데이터 쓰기 실패", e);
                }
            });
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }

        log.info("직원 백업 파일 저장 완료: {}", filePath);
        return filePath.toString();
    }

    /**
     * 파일 삭제 메서드
     * @param filePath 삭제할 파일 경로
     * @return 삭제 성공 여부
     */
    public boolean deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            boolean deleted = Files.deleteIfExists(path);
            if (deleted) {
                log.info("파일 삭제 완료: {}", filePath);
            } else {
                log.info("파일이 존재하지 않음: {}", filePath);
            }
            return deleted;
        } catch (IOException e) {
            log.error("파일 삭제 실패: {}", filePath, e);
            return false;
        }
    }

    /**
     * 백업 실패 시 에러 로그를 저장하는 메서드
     * @return 저장된 .log 파일 경로, 실패시 null
     */
    public String saveErrorLog(Exception e) {
        String filename = "error_log_" + FILE_TIMESTAMP_FORMAT.format(Instant.now()) + ".log";
        Path errorFilePath = errorLogDir.resolve(filename);

        try (BufferedWriter writer = Files.newBufferedWriter(errorFilePath, StandardCharsets.UTF_8)) {
            writer.write("ERROR 백업 실패: " + Instant.now());
            writer.newLine();

            writer.write("메시지: " + (e.getMessage() != null ? e.getMessage() : "없음"));
            writer.newLine();
            for (StackTraceElement se : e.getStackTrace()) {
                writer.write(" "+se.toString());
                writer.newLine();
            }

            Throwable cause = e.getCause();
            if (cause != null) {
                writer.write("cause exception: " + cause.getClass().getName());
                writer.newLine();
                writer.write("cause message: " + (cause.getMessage() != null ? cause.getMessage() : "없음"));
                writer.newLine();
            }

            log.info("error log saved: {}", errorFilePath);
            return errorFilePath.toString();
        } catch (IOException ioException) {
            log.error("fail to save error log file", ioException);
            return null;
        }
    }
}
