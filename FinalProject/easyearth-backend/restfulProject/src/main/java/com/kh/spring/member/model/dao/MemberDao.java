package com.kh.spring.member.model.dao;

import com.kh.spring.member.model.vo.MemberDetailVO;
import com.kh.spring.member.model.vo.MemberWalletVO;
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import com.kh.spring.member.model.vo.MemberVO;

import java.util.List;

@Repository
public class MemberDao {
	
	//회원가입
	public int insertMember(SqlSession sqlSession, MemberVO m) {
		
		return sqlSession.insert("memberMapper.insertMember",m);
	}
	
	//아이디 중복체크
	public int checkId(SqlSession sqlSession, String loginId) {
		
		return sqlSession.selectOne("memberMapper.checkId",loginId);
	}

	//로그인
	public MemberVO loginMember(SqlSession sqlSession, MemberVO m) {
		
		return sqlSession.selectOne("memberMapper.loginMember",m);
	}
	
	//비밀번호 찾기
	public MemberVO findPassword(SqlSession sqlSession, MemberVO m) {
		
		return sqlSession.selectOne("memberMapper.findPassword",m);
	}
	
	//회원 정보 수정
	public int updateMember(SqlSession sqlSession, MemberVO m) {
		
		return sqlSession.update("memberMapper.updateMember",m);
	}

	//회원 탈퇴
	public int deleteMember(SqlSession sqlSession, String loginId) {
		
		return sqlSession.delete("memberMapper.deleteMember",loginId);
	}

	//회원 정보 조회
	public MemberVO selectMemberById(SqlSession sqlSession, int memberId) {
		
		return sqlSession.selectOne("memberMapper.selectMemberById",memberId);
	}


    public List<Integer> equippedItem(SqlSession sqlSession, String memberId) {
		return sqlSession.selectList("memberMapper.equippedItem", memberId);
    }

	public MemberWalletVO getMemberPoint(SqlSession sqlSession, int memberId) {
		return sqlSession.selectOne("memberMapper.getMemberPoint", memberId);
	}


    public MemberDetailVO getMemberDetail(SqlSession sqlSession, int memberId) {
		return sqlSession.selectOne("memberMapper.getMemberDetail",memberId);
    }
    
    // 온라인 상태 업데이트
    public int updateOnlineStatus(SqlSession sqlSession, int memberId, int isOnline) {
    	java.util.HashMap<String, Object> map = new java.util.HashMap<>();
    	map.put("memberId", memberId);
    	map.put("isOnline", isOnline);
    	return sqlSession.update("memberMapper.updateOnlineStatus", map);
    }
}
