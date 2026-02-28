package com.kh.spring.ecoshop.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;



@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Review {
//    ESR_ID	NUMBER
//    SHOP_ID	NUMBER
//    RATING	NUMBER
//    CONTENT	CLOB
//    CREATED_AT	DATE
//    UPDATED_AT	DATE
//    MEMBER_ID	NUMBER
    int esrId;
    int shopId;
    int rating;
    String content;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private java.util.Date createdAt;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private java.util.Date updateAt;
    int memberId;
    private String contsId;
}
