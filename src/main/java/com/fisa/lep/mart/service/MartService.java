package com.fisa.lep.mart.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fisa.lep.area.dto.request.RequestAreaDTO;
import com.fisa.lep.area.dto.request.RequestAreaInfoDTO;
import com.fisa.lep.area.entity.Area;
import com.fisa.lep.area.repository.AreaRepository;
import com.fisa.lep.inventory.entity.Inventory;
import com.fisa.lep.inventory.repository.InventoryRepository;
import com.fisa.lep.mart.entity.Mart;
import com.fisa.lep.mart.repository.MartRepository;
import com.fisa.lep.product.entity.Product;
import com.fisa.lep.product.repository.ProductRepository;
import com.opencsv.CSVReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.*;
import java.math.BigDecimal;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class MartService {

    @Value("${API-KEY.apiKey}")
    String apiKey;

    @Value("${API-KEY.clientId}")
    String clientId;

    @Value("${API-KEY.clientSecret}")
    String clientSecret;

    private final MartRepository martRepository;
    private final AreaRepository areaRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    //    @Transactional
    public void insertData(String csvFilePath) {
        try (CSVReader csvReader = new CSVReader(new InputStreamReader(new FileInputStream(csvFilePath), StandardCharsets.UTF_8))) {
//            try(CSVReader csvReader = new CSVReader(new FileReader(csvFilePath, StandardCharsets.UTF_8))) {
            String[] values;
            log.info("values : {}", values = csvReader.readNext());

            while((values = csvReader.readNext()) != null) {
                String name = values[3]; // 판매 업소

                Mart mart = null;

                if(!martRepository.existsByName(name)) {
                    String address = naverSearchApi(name); // 서울특별시 중구 수표동 99

                    if (address == null) {
                        log.error("Address not found for martName: {}", name);
                        continue; // 주소를 찾을 수 없는 경우 이 항목을 건너뛰기
                    }

                    RequestAreaDTO areaDTO = getKakaoApiFromAddress(address);

                    if (areaDTO == null) {
                        log.error("AreaDTO not found for address: {}", address);
                        continue; // AreaDTO를 찾을 수 없는 경우 이 항목을 건너뛰기
                    }

//                    Area area = null;

//                    if(!areaRepository.existsByZoneNo(areaDTO.getZoneNo())) {
//                        area = areaRepository.save(Area.saveArea(areaDTO));
//                    } else { // 이미 같은 우편번호가 있을 때
//                        area = areaRepository.findByZoneNo(areaDTO.getZoneNo());
//                    }

                    Area area = areaRepository.findByHjdCode(areaDTO.getHjdCode());
                    if (area == null) {
                        area = areaRepository.save(Area.saveArea(areaDTO));
                    }

//                    Brand brand;

//                    try {
//                        brand = Brand.valueOf(values[7].toUpperCase());
//                    } catch (IllegalArgumentException | NullPointerException e) {
//                        log.warn("Invalid brand: {}. Setting default brand to NH.", values[7]);
//                        brand = Brand.NONE;
//                    }

                    mart = new Mart(name, area);
                    martRepository.save(mart);
                } else {
                    mart = martRepository.findByName(name).orElse(null);
                }
//Thread.sleep(100);
                Product product = null;

                if (!productRepository.existsByName(values[0])) {
                    product = Product.builder()
                            .name(values[0])
                            .company(values[4])
                            .build();

                    productRepository.save(product);
                } else {
                    product = productRepository.findByName(values[0]).orElse(null);
                }

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate checkDate = LocalDate.parse(values[1], formatter);
                BigDecimal price = new BigDecimal(values[2]);

                Inventory inventory = new Inventory(checkDate, price, mart, product);
                inventoryRepository.save(inventory);
            }
        } catch(Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 네이버 검색 API
     * */
    public String naverSearchApi(String martName) throws JsonProcessingException {
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

//        log.info("응답 : {}", responseBody);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);

//        String address = jsonNode.get("items").get(0).get("address").asText();
////        log.info("주소: {}", address);
//        return address;
        JsonNode items = jsonNode.get("items");
        if (items != null && items.isArray() && items.size() > 0) {
            String address = items.get(0).get("address").asText();
            return address;
        } else {
            log.error("No items found in Naver API response for martName: {}", martName);
            return null; // 혹은 기본값을 반환하거나, 예외를 던질 수 있습니다.
        }
    }

    /**
     * Request : 서울특별시 중구 수표동 99
     * Response : Mart에 insert할 정보들(시, 구, 동, 우편번호)
     * */
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

    @Transactional(readOnly = true)
    public void findLowestPrice(RequestAreaInfoDTO areaInfoDTO) {
        //        private String fullAddr; // 서울 중구 수표동 99

        // 시나리오 1. 서울 지역 내에서 가장 저렴한 곳 찾기 - OO시에서 찾기
        // request : depth 1에서 찾기 카테고리, 서울

        RequestAreaDTO requestAreaDTO = getKakaoApiFromAddress(areaInfoDTO.getFullAddr());
        requestAreaDTO.getRegion1depthName();

//        List<Mart> martsInArea = martRepository.findByAreaName(requestAreaDTO.getRegion1depthName());
//        Product lowestPriceProduct = null;
//
//        for (Mart mart : martsInArea) {
//            List<Product> products = productRepository.findByMartAndName(mart, areaInfoDTO.getProductName());
//            for (Product product : products) {
//                // inventory로 바꿔야 함
////                if (lowestPriceProduct == null || product.getPrice().compareTo(lowestPriceProduct.getPrice()) < 0) {
////                    lowestPriceProduct = product;
////                }
//            }
//        }

//        if (lowestPriceProduct != null) {
//            return lowestPriceProduct.getMart();
//        }
//
//        return null;

//        areaRepository.findByFullAddr(areaInfoDTO.getFullAddr());

    }
}
