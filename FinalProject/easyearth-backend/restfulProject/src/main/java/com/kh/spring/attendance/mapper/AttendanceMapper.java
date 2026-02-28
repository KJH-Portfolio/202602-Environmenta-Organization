package com.kh.spring.attendance.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import com.kh.spring.attendance.model.vo.Attendance;

@Mapper
public interface AttendanceMapper {

    // 오늘 출석 여부 확인
    int countAttendanceToday(int memberId);

    // 가장 최근 출석 기록 조회
    Attendance findLastAttendanceByUserId(int memberId);

    // 출석 기록 저장
    int insertAttendance(Attendance attendance);

    // 포인트 지급 (POINT_WALLET 업데이트)
    int updatePoint(Map<String, Object> params);

    // 이번 달 출석 기록 조회 (캘린더용)
    List<Attendance> findAttendanceHistoryByMonth(Map<String, Object> params);

    // 테스트용 포인트 증률 (누적 포인트 포함)
    int addPointsForTesting(Map<String, Object> params);

    // 테스트용 포인트 증률 (로그인 아이디 기준)
    int addPointsByLoginIdForTesting(Map<String, Object> params);
}
