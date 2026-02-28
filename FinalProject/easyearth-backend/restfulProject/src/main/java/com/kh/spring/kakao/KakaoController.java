package com.kh.spring.kakao;

import com.kh.spring.member.model.service.MemberService;
import com.kh.spring.member.model.vo.MemberVO;
import com.kh.spring.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class KakaoController {

    private final KakaoService kakaoService;
    private final MemberService memberService;
    private final JWTUtil jwtUtil;

    @GetMapping("/api/auth/kakao")
    public ResponseEntity<?> kakaoCallback(@RequestParam String code) {
        // 1. 카카오 토큰 및 정보 가져오기
        String accessToken = kakaoService.getAccessToken(code);
        Map<String, Object> userInfo = kakaoService.getUserInfo(accessToken);

        // 2. 카카오 고유 ID(loginId로 활용) 및 닉네임 추출
        String kakaoId = userInfo.get("id").toString();
        Map<String, Object> properties = (Map<String, Object>) userInfo.get("properties");
        String nickname = (properties != null) ? properties.get("nickname").toString() : "";

        // 3. DB에서 해당 아이디로 회원 조회
        // memberService.loginMember가 loginId로 조회하는 로직을 활용
        MemberVO member = memberService.loginMember(MemberVO.builder().loginId(kakaoId).build());

        Map<String, Object> result = new HashMap<>();

        if (member != null) {
            // [CASE 1] 이미 가입된 회원 -> 바로 로그인 처리 (JWT 발급)
            String token = jwtUtil.generateToken(member.getLoginId());
            member.setPassword(null); // 보안

            result.put("isMember", true);
            result.put("token", token);
            result.put("user", member);
        } else {
            // [CASE 2] 미가입 회원 -> 정보를 담아 프론트로 응답
            // DB 저장은 하지 않고 프론트에서 회원가입 페이지로 유도하게 함
            result.put("isMember", false);
            result.put("kakaoId", kakaoId);
            result.put("nickname", nickname);
        }

        return ResponseEntity.ok(result);
    }
}