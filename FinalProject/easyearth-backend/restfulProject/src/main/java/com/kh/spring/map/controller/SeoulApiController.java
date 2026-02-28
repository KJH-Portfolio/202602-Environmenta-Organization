package com.kh.spring.map.controller;

import com.kh.spring.map.service.SeoulMapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seoul")
@RequiredArgsConstructor
@Tag(name = "서울맵 API", description = "서울맵 API(필터링 / 상세조회)")
public class SeoulApiController {

    private final SeoulMapService seoulMapService;


    @Operation(
            summary = "테마 필터링",
            description = "테마 / 거리(m) / 키워드를 지정하는 API (기준 좌표: 126.974695, 37.564150)\n\n" +
                    "[테마 코드 목록]\n" +
                    "• 공중화장실: 100106\n" + //데이터 없음
                    "• 서울 야경명소: 1741228380725\n" + //COT_VALUE_01":"연중무휴", "COT_VALUE_03":"무료"
                    "• (친환경)자전거 도로: 1657588761062\n" + //데이터 없음

                    //"COT_VALUE_03":"일반결제 시에도 300원 상당의 할인을 제공\n(*해당 금액은 시가 매장에 사후 정산하는 방식으로 운영)"
                    //"COT_VALUE_01":"①서울페이 회원가입→ ②개인컵 지참하여 음료주문→ ③서울페이 앱으로 결제→ ④1주 후 포인트 적립"
                    //"COT_NAME_03":"서울페이 사용이  어려운 시민에게는..."
                    //"COT_KW":"#개인컵, #개인 컵, #텀블러, #환경, #에코"
                    "• [착한소비] 개인 컵 할인 카페: 1693986134109\n" +
                    "• 서울 조경상: 1756118598433\n" + //데이터 없음

                    //"COT_VALUE_01":"매일 지나다니는 출퇴근 거리가 어느 순간 알록달록 꽃밭이 되었습니다. 일상의 지루함을 날리고 걸을때마다 소소한 즐거움을 느낄 수 있는 가로수 아래 한 뼘 정원이 시민들 곁으로 다가갑니다.
                    //계절의 변화에 맞춰 물들어가는 가로수 아래 작은 공간을 기억해 주세요."
                    //COT_IMG_MAIN_URL":"/smgis2/file/ucimgs/conts/1723624111751/가로수 아래 한뼘 매력가든_1.jpg"
                    "• 매력가든‧동행가든: 1723624111751\n" +

                    //COT_COORD_DATA":"[[126.95375910351606,37.51236718649842],[126.95330726249287,37.511486275754045]
                    //"COT_IMG_MAIN_URL":"/smgis2/file/ucimgs/conts/1681179124353/walk_07_0_KOR.jpg"
                    "• 기발한 별지도(운동명소): 1681179124353\n" +

                    //COT_COORD_DATA":"[[[126.93869259427113,37.58524625474576],[126.93858627076027,37.58526490261575]
                    "• 기발한 별지도 (키즈편): 1684396695589\n" +
                    "• [착한소비] 제로웨이스트상점: 11103395\n" +
                    "• 폐의약품 전용수거함 위치: 1649132420936\n" +
                    "• 폐건전지 폐형광등 분리수거함: 11103389\n" +

                    //"COT_COORD_DATA":"[[126.96879541382862,37.55647804540249],[126.96880980489462,37.556843156068254]
                    "• 서울로 트레킹 길: 11102818\n" +
                    "• 녹색교통지역: 11101339\n" +

                    //데이터 없음
                    "• 서울로7017: 11101096\n" +

                    //"COT_VALUE_03":"2020. 4~11월"
                    "• 생물다양성 현황: 11101836\n" +
                    "• 사진속서울: 100273\n" +
                    "• 우리동네약수터: 100578\n" +
                    "• 서울시 자원회수시설: 100771\n" +
                    "• 폐기물 관련 환경기초시설(공공): 11101844\n" +

                    //"COT_VALUE_01" : "0"
                    "• [성북] 규격(특수)봉투 판매소: 1765960848355\n" +

                    //"COT_VALUE_01" : "-"
                    "• [성북] 도심공원 즐기기: 1765960271640\n" +
                    "• 용산구 재활용 의류수거함 정보: 1755739898113\n" +

                    //"COT_COORD_DATA":"[[[127.02641228623499,37.626241688780794],[127.028471474175,37.626768203957134]
                    "• 강북감성길(놀러와 우리동네): 1741305884013\n" +

                    //데이터 없음
                    "• 재활용 정거장: 1730359504536\n" +
                    "• 서울형 치유의 숲길 조성: 1732335864484\n" +

                    //"COT_COORD_DATA":"[[[126.973595,37.565277],[126.97362,37.565075]
                    "• 차 없는 거리: 11101181\n" +
                    "• 구로, 공원 함께 가요!: 1709085904880\n" +

                    //데이터 없음
                    "• 구로구 반려동물 동행: 1698837393344\n" +

                    //데이터 없음
                    "• 서대문구 안전산책로: 1692774403603\n" +
                    "• 구로구 안양천명소 주요시설물: 1663564614080\n" +

                    //데이터 없음
                    "• 관악구 기후변화자원지도: 100732"
    )


    @GetMapping("/themes/contents")
    public String theme(
            @RequestParam List<String> themeIds,
            @RequestParam(required = false, defaultValue = "126.974695") Double x,
            @RequestParam(required = false, defaultValue = "37.564150") Double y,
            @RequestParam(required = false, defaultValue = "2000") Integer distance,
            @RequestParam(required = false) String keyword
    ) {
        return seoulMapService.getFilteredMapData(themeIds, x, y, distance, keyword);
    }

    @Operation(summary = "상세정보 조회 (데이터베이스 저장)", description = "가게 상세정보 조회하는 API ex). zerowaste_0054")
    @GetMapping("/detail")
    public String detail(
            @RequestParam(defaultValue = "11103395") String themeId,
            @RequestParam(defaultValue = "zerowaste_0054") String contsId
    ) {
        return seoulMapService.getDetail(themeId, contsId);
    }
}
