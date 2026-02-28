package com.kh.spring.weather.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UvDto {
    /** 1. 관측시각 (KST, 년월일시분) */
    private String tm;

    /** 2. 자외선 관측 지점 ID */
    private Integer stn;

    /** 3. 자외선 관측(MED/10min) */
    private Double uvb;

    /** 4. 자외선 관측(J/cm2/10min) */
    private Double uva;

    /** 5. 자외선 관측(MED/10min), -999(NULL) */
    private Double euv;

    /** 6. 자외선 지수 (UV-B) */
    private Double uvBIndex;

    /** 7. 자외선 지수 (UV-A) */
    private Double uvAIndex;

    /** 8. 내부온도1 */
    private Double temp1;

    /** 9. 내부온도2 */
    private Double temp2;
}
