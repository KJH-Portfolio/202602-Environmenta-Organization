package com.kh.spring.chat.model.vo;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Getter
@Table(name="MEMBER")
public class MemberEntity {
	
	//프로텍트 설정 - 롬북 오류로 인해 수동 지정
	protected MemberEntity() {}
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MEMBER_ID")
    private Long id;

    @Column(name = "LOGIN_ID", nullable = false, unique = true, length = 50)
    private String loginId;

    @Column(name = "PASSWORD", nullable = false, length = 100)
    private String password;

    @Column(name = "NAME", nullable = false, length = 30)
    private String name;

    @Column(name = "BIRTHDAY", length = 10)
    private String birthday;

    @Column(name = "GENDER", length = 10)
    private String gender;

    @Column(name = "ADDRESS", length = 200)
    private String address;

    @Column(name = "STATUS", length = 20)
    private String status;

    @Column(name = "QUIZ_ATTEMPT_COUNT")
    private Integer quizAttemptCount;

    @Column(name = "QUIZ_CORRECT_COUNT")
    private Integer quizCorrectCount;

    // --- 카카오톡 기능 ---
    @Lob
    @Column(name = "PROFILE_IMAGE_URL")
    private String profileImageUrl;

    @Column(name = "STATUS_MESSAGE")
    private String statusMessage;

    @Column(name = "IS_ONLINE", columnDefinition = "NUMBER(1) DEFAULT 0")
    private Integer isOnline;

    @CreationTimestamp
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UPDATE_AT", nullable = false)
    private LocalDateTime updatedAt;

    // 프로필 이미지 변경
    public void updateProfileImage(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
