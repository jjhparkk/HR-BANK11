package com.team11.hrbank.module.domain.file;

import com.team11.hrbank.module.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "files")
public class File extends BaseEntity {  // BaseEntity 상속받기

    @NotNull
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @NotNull
    @Column(name = "format", nullable = false, length = 50)
    private String format;

    @NotNull
    @Column(name = "file_path", nullable = false, length = 255)
    private String filePath;

    @NotNull
    @Column(name = "size", nullable = false)
    private Long size;  // 파일 크기 추가
}
