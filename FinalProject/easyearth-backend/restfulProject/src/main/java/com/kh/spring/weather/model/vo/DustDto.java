package com.kh.spring.weather.model.vo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DustDto {
    /** 1. 관측시각 (KST, 년월일시분) - 예: 202601280000 */
    private String tm;
    
    /** 2. 지점번호 - 예: 108 */
    private int stnId;
    
    /** 3. 미세먼지 농도 (ug/m3) - 예: 41 */
    private int pm10;
    
    /** 4. 장비상태 - 예: 000000 */
    private String flag;
    
    /** 5. MQC 결과 */
    private String mqc;
}