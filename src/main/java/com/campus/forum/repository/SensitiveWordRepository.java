package com.campus.forum.repository;

import com.campus.forum.entity.SensitiveWord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SensitiveWordRepository extends JpaRepository<SensitiveWord, Long> {
    List<SensitiveWord> findByStatus(Integer status);
}