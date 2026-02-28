package com.kh.spring.member.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MemberWalletVO {
    int walletId;
    int memberId;
    int nowPoint;
    int totalEarnedPoint;
    int totalSpentPoint;
}
