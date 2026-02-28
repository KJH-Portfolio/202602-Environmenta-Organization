package com.kh.spring.ecotree.model.vo;

import java.sql.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EcoTreeVO {
    private int memberId;
    private int treeLevel;
    private long syncedExp; // 누적 포인트이므로 범위를 고려해 long 사용
    private Date lastGrowthDate;
    
    // 추가 정보 (UI 표시용)
    private long totalEarnedPoint; // POINT_WALLET에서 가져온 현재 총 누적 포인트
}
