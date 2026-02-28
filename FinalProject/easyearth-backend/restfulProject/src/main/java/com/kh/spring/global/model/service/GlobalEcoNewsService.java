package com.kh.spring.global.model.service;

import java.io.StringReader;

import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.kh.spring.common.service.FileCacheService;
import com.kh.spring.gemini.service.GeminiService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class GlobalEcoNewsService {

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private FileCacheService fileCacheService;

    // 캐시 파일명 (글로벌 뉴스 데이터 저장)
    private static final String CACHE_FILE = "news_global.json";
    
    // NYT(New York Times) RSS URL 목록 (기후, 환경 관련)
    private static final List<String> RSS_URLS = List.of(
            "https://rss.nytimes.com/services/xml/rss/nyt/Climate.xml",
            "https://rss.nytimes.com/services/xml/rss/nyt/Weather.xml"
    );

    // 뉴스 조회 캐시 데이터 반환 (없으면 갱신)
    public String getGlobalEcoNews() {
        // 캐시 확인 (String 타입으로 로드)
        String cached = fileCacheService.load(CACHE_FILE, String.class);
        
        // 캐시가 존재하고 비어있지 않은 경우
        if (cached != null && !cached.isEmpty()) {
            // 구버전 캐시(배열 '[')인 경우 호환되지 않으므로 갱신
            if (cached.trim().startsWith("[")) {
                log.info(">>> 구버전 뉴스 캐시 감지. 갱신을 수행합니다.");
                return refreshGlobalNews();
            }
            return cached;
        }
        // 캐시 없으면 갱신 로직 실행
        return refreshGlobalNews();
    }

    // 뉴스 갱신 API 호출 및 파일 저장
    public String refreshGlobalNews() {
        log.info(">>> 글로벌 환경 뉴스 갱신 시작...");
        String newData = fetchGlobalNewsFromApi();
        
        // 유효한 데이터가 있을 경우에만 캐시에 저장
        if (newData != null && !newData.equals("[]") && !newData.trim().isEmpty()) {
            fileCacheService.save(CACHE_FILE, newData);
            log.info(">>> 글로벌 환경 뉴스 갱신 완료 및 저장.");
        } else {
            log.warn(">>> 글로벌 환경 뉴스 갱신 실패 (데이터 없음).");
        }
        return newData;
    }

    // 뉴스 수집 NYT RSS 파싱 -> Gemini 번역/요약
    private String fetchGlobalNewsFromApi() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            // 최종 결과를 담을 StringBuilder (JSON Object 시작)
            StringBuilder finalJsonBuilder = new StringBuilder();
            finalJsonBuilder.append("{\n");
            
            String[] categories = {"Climate", "Weather"};

            // 3개의 RSS URL 순회
            for (int i = 0; i < RSS_URLS.size(); i++) {
                String url = RSS_URLS.get(i);
                String category = categories[i];
                
                // 각 카테고리별 뉴스 텍스트를 모을 빌더
                StringBuilder categoryNewsBuilder = new StringBuilder();
                
                try {
                    log.info(">>> [{}] RSS Fetching: {}", category, url);
                    String rssContent = restTemplate.getForObject(url, String.class);
                    
                    if (rssContent != null) {
                        InputSource is = new InputSource(new StringReader(rssContent));
                        Document doc = builder.parse(is);
                        doc.getDocumentElement().normalize();
                        
                        NodeList itemList = doc.getElementsByTagName("item");
                        int count = 0;
                        
                        // 각 카테고리별 상위 5개만 추출
                        for (int j = 0; j < itemList.getLength() && count < 5; j++) {
                            Node node = itemList.item(j);
                            if (node.getNodeType() == Node.ELEMENT_NODE) {
                                Element element = (Element) node;
                                String title = getTagValue("title", element);
                                String desc = getTagValue("description", element);
                                
                                // 설명이 너무 길면 자름 (토큰 절약)
                                if (desc != null && desc.length() > 150) {
                                    desc = desc.substring(0, 150) + "...";
                                }
                                String link = getTagValue("link", element);
                                
                                // 이미지 URL 추출
                                String imageUrl = "";
                                NodeList mediaList = element.getElementsByTagName("media:content");
                                if (mediaList != null && mediaList.getLength() > 0) {
                                    Element media = (Element) mediaList.item(0);
                                    if (media.hasAttribute("url")) {
                                        imageUrl = media.getAttribute("url");
                                    }
                                }
                                
                                // 이미지가 없는 기사는 제외
                                if (imageUrl == null || imageUrl.isEmpty()) {
                                    continue;
                                }

                                // AI에게 보낼 텍스트 구성
                                categoryNewsBuilder.append(String.format("Title: %s\nSummary: %s\nLink: %s\nImage: %s\n---\n", title, desc, link, imageUrl));
                                count++;
                            }
                        }
                    }
                    
                    // 해당 카테고리의 뉴스 텍스트가 준비되었으면 Gemini 호출
                    String categoryText = categoryNewsBuilder.toString();
                    log.info(">>> [{}] Sending to Gemini...", category);
                    String categoryJsonArray = geminiService.getNewsByCategory(category, categoryText);
                    
                    // 빈 배열이면 기본값 처리
                    if (categoryJsonArray == null || categoryJsonArray.isEmpty() || categoryJsonArray.equals("[]")) {
                        categoryJsonArray = "[]";
                    }
                    
                    // 최종 JSON에 키 추가
                    finalJsonBuilder.append("\"").append(category).append("\": ").append(categoryJsonArray);
                    
                    // 마지막 항목이 아니면 콤마 추가
                    if (i < RSS_URLS.size() - 1) {
                        finalJsonBuilder.append(",\n");
                    } else {
                        finalJsonBuilder.append("\n");
                    }
                    
                } catch (Exception e) {
                    log.error("Error processing category " + category, e);
                    // 에러 시 빈 배열 추가 (JSON 깨짐 방지)
                    finalJsonBuilder.append("\"").append(category).append("\": []");
                    if (i < RSS_URLS.size() - 1) finalJsonBuilder.append(",\n");
                }
            }
            
            finalJsonBuilder.append("}");
            return finalJsonBuilder.toString();

        } catch (Exception e) {
            log.error("Global News Generation Failed", e);
            return "{\"Climate\":[], \"Weather\":[]}";
        }
    }

    // Helper XML 태그 값 추출
    private String getTagValue(String tag, Element element) {
        NodeList nodeList = element.getElementsByTagName(tag);
        if (nodeList != null && nodeList.getLength() > 0) {
            NodeList childNodes = nodeList.item(0).getChildNodes();
            if (childNodes != null && childNodes.getLength() > 0) {
                Node node = childNodes.item(0);
                return node.getNodeValue();
            }
        }
        return "";
    }
}
