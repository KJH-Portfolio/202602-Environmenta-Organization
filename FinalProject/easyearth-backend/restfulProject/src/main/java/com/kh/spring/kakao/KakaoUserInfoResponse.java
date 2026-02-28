package com.kh.spring.kakao;

import lombok.Getter;

@Getter
public class KakaoUserInfoResponse {
    private Long id;
    private KakaoAccount kakao_account;

    @Getter
    public static class KakaoAccount {
        private String email;
        private Profile profile;
    }

    @Getter
    public static class Profile {
        private String nickname;
        private String profile_image_url;
    }
}