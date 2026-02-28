package com.kh.spring.attendance.service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.spring.attendance.mapper.AttendanceMapper;
import com.kh.spring.attendance.model.vo.Attendance;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceMapper attendanceMapper;

    /**
     * 출석 체크 본 기능
     * 
     * @param memberId
     * @return 획득한 포인트 (이미 출석 시 -1 반환)
     */
    @Transactional
    public int checkAttendance(int memberId) {

        // 1. 오늘 이미 출석했는지 확인 (하루 1회 제한)
        int count = attendanceMapper.countAttendanceToday(memberId);
        if (count > 0) {
            return -1; // 이미 출석함
        }

        // 2. 어제 출석 기록 확인 (연속 출석 판단)
        Attendance lastAttendance = attendanceMapper.findLastAttendanceByUserId(memberId);

        int consecutiveDays = 1;

        if (lastAttendance != null) {
            LocalDate lastDate = lastAttendance.getAttendanceDate().toLocalDate();
            LocalDate today = LocalDate.now();

            // 어제 출석했으면 연속 일수 +1, 아니면 1일차로 초기화
            if (lastDate.equals(today.minusDays(1))) {
                consecutiveDays = lastAttendance.getConsecutiveDays() + 1;
            }
        }

        // 3. 포인트 계산
        int points = 0;

        // 기본 포인트
        if (consecutiveDays <= 5) {
            points = 100;
        } else {
            points = 150;
        }

        // 보너스 포인트 (15일차, 30일차)
        if (consecutiveDays == 15) {
            points += 250;
        } else if (consecutiveDays == 30) {
            points += 500;
        }

        // 4. 출석 기록 저장
        Attendance newAttendance = Attendance.builder()
                .userId(memberId)
                .consecutiveDays(consecutiveDays)
                .pointsEarned(points)
                .build();

        attendanceMapper.insertAttendance(newAttendance);

        // 5. 포인트 지갑 업데이트
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("memberId", memberId);
        paramMap.put("points", points);
        attendanceMapper.updatePoint(paramMap);

        return points;
    }

    /**
     * 이번 달 출석 현황 조회
     */
    public List<Attendance> getAttendanceHistory(int memberId, String yearMonth) {
        Map<String, Object> params = new HashMap<>();
        params.put("memberId", memberId);
        params.put("yearMonth", yearMonth);
        return attendanceMapper.findAttendanceHistoryByMonth(params);
    }

    /**
     * [테스트용] 포인트 강제 지급
     */
    @Transactional
    public boolean grantAdminPoints(int memberId, int points) {
        Map<String, Object> params = new HashMap<>();
        params.put("memberId", memberId);
        params.put("points", points);
        return attendanceMapper.addPointsForTesting(params) > 0;
    }

    /**
     * [테스트용] 로그인 아이디로 포인트 강제 지급
     */
    @Transactional
    public boolean grantAdminPointsByLoginId(String loginId, int points) {
        Map<String, Object> params = new HashMap<>();
        params.put("loginId", loginId);
        params.put("points", points);
        return attendanceMapper.addPointsByLoginIdForTesting(params) > 0;
    }}

    
    

    