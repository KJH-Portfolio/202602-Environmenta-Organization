package com.kh.spring.chat.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.kh.spring.chat.model.vo.MemberEntity;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<MemberEntity, Long> {
    Optional<MemberEntity> findByLoginId(String loginId);
    List<MemberEntity> findByNameContaining(String name);
    List<MemberEntity> findByNameContainingOrLoginIdContaining(String name, String loginId);

    List<MemberEntity> findByName(String name);
}
