package com.fisa.lep.mart.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fisa.lep.area.dto.request.RequestAreaDTO;
import com.fisa.lep.area.dto.request.RequestAreaInfoDTO;
import com.fisa.lep.mart.service.MartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.charset.Charset;

@RestController
@RequestMapping("/api/mart")
@Slf4j
@RequiredArgsConstructor
public class MartController {

    @Value("${API-KEY.apiKey}")
    String apiKey;

    @Value("${API-KEY.clientId}")
    String clientId;

    @Value("${API-KEY.clientSecret}")
    String clientSecret;

    private final MartService martService;

    /**
     * csv 읽어와서 naver, kakako api 거쳐서 Mart, Area에 데이터 insert
     * */
    @GetMapping("")
    public ResponseEntity<?> insertData(@RequestParam String path) {
        String csvPath = "src/main/resources/csv/한국소비자원_생필품 가격 정보_20240517 (1) - 복사본.csv";
//        String csvPath = "src/main/resources/csv/통합 문서1.csv";
        martService.insertData(csvPath);


        return null;
    }

    /**
     * Request : 세븐일레븐(본사)
     * Response : 서울특별시 중구 수표동 99
     * */
    @GetMapping("/map")
    public ResponseEntity<?> naverSearchApi(@RequestParam String martName) throws JsonProcessingException {
        URI uri = UriComponentsBuilder
                .fromUriString("https://openapi.naver.com")
                .path("/v1/search/local.json")
                .queryParam("query", martName)
                .queryParam("display", 1)
                .queryParam("start", 1)
                .queryParam("sort", "random")
                .encode(Charset.forName("UTF-8"))
                .build()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", clientId);
        headers.set("X-Naver-Client-Secret", clientSecret);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, requestEntity, String.class);

        String responseBody = responseEntity.getBody();

        log.info("응답 : {}", responseBody);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);

        String address = jsonNode.get("items").get(0).get("address").asText();
        log.info("주소: {}", address);

        return ResponseEntity.ok(responseBody);
    }

    /**
     * Request : 서울특별시 중구 수표동 99
     * Response : Mart에 insert할 정보들(시, 구, 동, 우편번호)
     * */
    @GetMapping("/kakao")
    public RequestAreaDTO getKakaoApiFromAddress(String roadFullAddr) {
        String apiUrl = "https://dapi.kakao.com/v2/local/search/address.json";
        String jsonString = null;

        try {
            roadFullAddr = URLEncoder.encode(roadFullAddr, "UTF-8");

            String addr = apiUrl + "?query=" + roadFullAddr;

            URL url = new URL(addr);
            URLConnection conn = url.openConnection();
            conn.setRequestProperty("Authorization", "KakaoAK " + apiKey);

            BufferedReader rd = null;
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuffer docJson = new StringBuffer();

            String line;

            while ((line=rd.readLine()) != null) {
                docJson.append(line);
            }

            jsonString = docJson.toString();
            rd.close();


            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonString);
            JsonNode documents = rootNode.path("documents");

            if (documents.isArray() && documents.size() > 0) {
                JsonNode addressInfo = documents.get(0).path("address");
                JsonNode roadAddressInfo = documents.get(0).path("road_address");

                String region1DepthName = addressInfo.path("region_1depth_name").asText();
                String region3DepthHName = addressInfo.path("region_3depth_h_name").asText();
                String zoneNo = roadAddressInfo.path("zone_no").asText();

                return RequestAreaDTO.builder()
                        .fullAddr(addressInfo.path("address_name").asText())
                        .region1depthName(addressInfo.path("region_1depth_name").asText())
                        .region2depthName(addressInfo.path("region_2depth_name").asText())
                        .region3depthName(addressInfo.path("region_3depth_name").asText())
                        .region3depthHName(addressInfo.path("region_3depth_h_name").asText())
                        .hjdCode(addressInfo.path("h_code").asText())
                        .zoneNo(roadAddressInfo.path("zone_no").asText()).build();
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 여기까지 insert했고, 이제 사용자가 입력하면 최대 열 개까지의 점포 보여주기

//     시나리오 1. 서울 지역 내에서 가장 저렴한 곳 찾기 - OO시에서 찾기
//     request : depth 1에서 찾기 카테고리, 서울
//
//     시나리오 2. 마포구 내에서 가장 저렴한 곳 찾기 - OO구에서 찾기
//     request : depth 2에서 찾기 카테고리, 서울, 중구
//
//     시나리오 3. 행정동 내에서 가장 저렴한 곳 찾기 - 내 동네에서 찾기
//     request : depth 3에서 찾기 카테고리, 서울, 중구, 명동
//
//     결론 : 사용자에게 입력받게 하면 귀찮으니까 내 위치 기반 찾아주기
//     request : 내 위치 fullAddr, depth가 몇인지
//     response : depth 기반 최저가 최대 열 군데 정보

    @PostMapping("/lowest")
    public ResponseEntity<?> findLowestPrice(@RequestBody RequestAreaInfoDTO areaInfoDTO) {


        return null;

    }
}
