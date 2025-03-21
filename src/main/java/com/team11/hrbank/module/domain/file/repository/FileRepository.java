package com.team11.hrbank.module.domain.file.repository;

import com.team11.hrbank.module.domain.file.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {
}
