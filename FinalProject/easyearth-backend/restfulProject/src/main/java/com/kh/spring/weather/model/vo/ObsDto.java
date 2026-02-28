package com.kh.spring.weather.model.vo;

import lombok.*;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ObsDto {

    @JsonProperty("tm")
    private String tm; // 1. 관측시각 (YYYYMMDDHHMI)

    @JsonProperty("stn")
    private Integer stn; // 2. 국내 지점번호

    @JsonProperty("wd")
    private Integer wd; // 3. 풍향 (36방위)

    @JsonProperty("ws")
    private Double ws; // 4. 풍속 (m/s)

    @JsonProperty("gst_wd")
    private Integer gstWd; // 5. 돌풍향 (36방위)

    @JsonProperty("gst_ws")
    private Double gstWs; // 6. 돌풍속 (m/s)

    @JsonProperty("gst_tm")
    private String gstTm; // 7. 돌풍속 관측시각 (시분)

    @JsonProperty("pa")
    private Double pa; // 8. 현지기압 (hPa)

    @JsonProperty("ps")
    private Double ps; // 9. 해면기압 (hPa)

    @JsonProperty("pt")
    private Integer pt; // 10. 기압변화경향 (Code 0200)

    @JsonProperty("pr")
    private Double pr; // 11. 기압변화량 (hPa)

    @JsonProperty("ta")
    private Double ta; // 12. 기온 (C)

    @JsonProperty("td")
    private Double td; // 13. 이슬점온도 (C)

    @JsonProperty("hm")
    private Double hm; // 14. 상대습도 (%)

    @JsonProperty("pv")
    private Double pv; // 15. 수증기압 (hPa)

    @JsonProperty("rn")
    private Double rn; // 16. 강수량 (mm)

    @JsonProperty("rn_day")
    private Double rnDay; // 17. 일강수량 (mm) - 통계표 기준

    @JsonProperty("rn_jun")
    private Double rnJun; // 18. 일강수량 (mm) - 전문 기준

    @JsonProperty("rn_int")
    private Double rnInt; // 19. 강수강도 (mm/h)

    @JsonProperty("sd_hr3")
    private Double sdHr3; // 20. 3시간 신적설 (cm)

    @JsonProperty("sd_day")
    private Double sdDay; // 21. 일 신적설 (cm)

    @JsonProperty("sd_tot")
    private Double sdTot; // 22. 적설 (cm)

    @JsonProperty("wc")
    private String wc; // 23. GTS 현재일기 (Code 4677)

    @JsonProperty("wp")
    private String wp; // 24. GTS 과거일기 (Code 4561)

    @JsonProperty("ww")
    private String ww; // 25. 국내식 일기코드

    @JsonProperty("ca_tot")
    private Integer caTot; // 26. 전운량 (1/10)

    @JsonProperty("ca_mid")
    private Integer caMid; // 27. 중하층운량 (1/10)

    @JsonProperty("ch_min")
    private Integer chMin; // 28. 최저운고 (100m)

    @JsonProperty("ct")
    private String ct; // 29. 운형

    @JsonProperty("ct_top")
    private String ctTop; // 30. GTS 상층운형 (Code 0509)

    @JsonProperty("ct_mid")
    private String ctMid; // 31. GTS 중층운형 (Code 0515)

    @JsonProperty("ct_low")
    private String ctLow; // 32. GTS 하층운형 (Code 0513)

    @JsonProperty("vs")
    private Integer vs; // 33. 시정 (10m)

    @JsonProperty("ss")
    private Double ss; // 34. 일조 (hr)

    @JsonProperty("si")
    private Double si; // 35. 일사 (MJ/m2)

    @JsonProperty("st_gd")
    private String stGd; // 36. 지면상태 코드

    @JsonProperty("ts")
    private Double ts; // 37. 지면온도 (C)

    @JsonProperty("te_005")
    private Double te005; // 38. 5cm 지중온도 (C)

    @JsonProperty("te_01")
    private Double te01; // 39. 10cm 지중온도 (C)

    @JsonProperty("te_02")
    private Double te02; // 40. 20cm 지중온도 (C)

    @JsonProperty("te_03")
    private Double te03; // 41. 30cm 지중온도 (C)

    @JsonProperty("st_sea")
    private String stSea; // 42. 해면상태 코드

    @JsonProperty("wh")
    private Double wh; // 43. 파고 (m)

    @JsonProperty("bf")
    private Integer bf; // 44. Beaufart 최대풍력

    @JsonProperty("ir")
    private Integer ir; // 45. 강수자료 유무 (Code 1819)

    @JsonProperty("ix")
    private Integer ix; // 46. 유인/무인 관측 여부 (Code 1860)
}