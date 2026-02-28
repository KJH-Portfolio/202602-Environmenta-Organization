package com.kh.spring.member.model.service;

import com.kh.spring.member.model.vo.MemberDetailVO;
import com.kh.spring.member.model.vo.MemberVO;
import com.kh.spring.member.model.vo.MemberWalletVO;

import java.util.List;

public interface MemberService {
	
	//회원가입
	int insertMember(MemberVO m);
	
	//아이디 중복체크
	int checkId(String loginId);
	
	//로그인
	MemberVO loginMember(MemberVO m);
	
	//비밀번호 찾기
	MemberVO findPassword(MemberVO m);
	
	//회원 정보 수정
	int updateMember(MemberVO m);
	
	//회원 탈퇴
	int deleteMember(String loginId);

	//회원 정보 조회
	MemberVO selectMemberById(int memberId);


    List<Integer> equippedItem(String memberId);

	MemberWalletVO getMemberPoint(int memberId);

  	//멤버 상세정보 조회
	MemberDetailVO getMemberDetail(int memberId);
	
	// 온라인 상태 업데이트
	int updateOnlineStatus(int memberId, int isOnline);
	
}
