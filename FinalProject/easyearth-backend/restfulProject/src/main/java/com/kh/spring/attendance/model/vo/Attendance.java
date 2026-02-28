package com.kh.spring.attendance.model.vo;

import java.sql.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance {
    private int attendanceId;
    private int userId; // Assuming int for userPK based on typical schema, can be String if needed
    private Date attendanceDate;
    private int consecutiveDays; // 연속 출석 일수
    private int pointsEarned; // 해당 출석으로 획득한 포인트
}
