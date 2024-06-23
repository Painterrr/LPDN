# Lowest-price-daily-necessities
생필품 최저가 검색 서비스

## 프로젝트 목적
DB Index를 활용하여 대용량 데이터 조회 시 검색 속도 개선

## 기간
2024.06

## 데이터
- 한국소비자원_생필품 가격 정보_20240517, 국토교통부_전국 법정동_20240513(출처: 공공데이터 포털)
- 한국소비자원_생필품 가격 정보_20240517 데이터

- 테이블 셋
- ![data_set](https://github.com/Painterrr/Lowest-price-daily-necessities-search-service/assets/98957340/ab812de4-d1b1-4099-ad2a-f9ff5275da27)

- 데이터 양(rows 수)
    area: 403
    mart: 459
    inventory: 1077442
    product: 102898

### 로직
- Request
  ```json
	{
    "productName": "머거본 알땅콩(135g)",
    "region1depthName": "서울",
    "region2depthName": "중구",
    "region3depthName": "수표동"
  }
  ```

- Response
    ```json
    [
        "2024-05-03",
        "2500.00",
        "머거본 알땅콩(135g)",
        "세븐일레븐(본사)",
        "",
        "서울 중구 수표동 99"
    ]
    ```

### 쿼리
  ```sql
    SELECT i.check_date, i.price, p.name AS product_name, m.name AS mart_name, m.brand AS mart_brand, a.full_addr
    FROM inventory i 
    JOIN product p ON i.product_id = p.product_id
    JOIN mart m ON i.mart_id = m.mart_id
    JOIN area a ON m.area_id = a.area_id
    WHERE a.region1depth_name = '서울'
      AND a.region2depth_name = '중구'
      AND a.region3depth_name = '수표동'
      AND p.name = '머거본 알땅콩(135g)'
    ORDER BY i.price ASC
    LIMIT 3;
  ```

## 인덱스

### 생성
- 작성된 쿼리를 기준으로 조인 컬럼, 조건 컬럼, 정렬 컬럼으로 인덱스 생성
    
    ```sql
    -- Mart 테이블의 조인 컬럼에 대한 인덱스
    CREATE INDEX idx_mart_name_brand ON mart (name, brand);
    
    -- Area 테이블의 조건 컬럼에 대한 복합 인덱스
    CREATE INDEX idx_area_region_depth ON area (region1depthName, region2depthName, region3depthName);
    
    -- Product 테이블의 조건 컬럼에 대한 인덱스
    CREATE INDEX idx_product_name ON product (name);
    
    -- Inventory 테이블의 정렬 컬럼에 대한 인덱스
    CREATE INDEX idx_inventory_price ON inventory (price);
    CREATE INDEX idx_inventory_date_product ON inventory (check_date, product_id);
    ```

## 프로젝트 수행 결과

### FULL SCAN
**평균: 5,067.1 ms (5초)**

### B+Tree 생성 후
**평균: 4,132.4 ms (4초)**

→ 조회 시간 1초 감소
→ 생각보다 개선이 별로 안돼서 Explain 돌려봄

```sql
  EXPLAIN SELECT i.check_date, i.price, p.name AS product_name, m.name AS mart_name, m.brand AS mart_brand, a.full_addr
  FROM inventory i
  JOIN product p ON i.product_id = p.product_id
  JOIN mart m ON i.mart_id = m.mart_id
  JOIN area a ON m.area_id = a.area_id
  WHERE a.region1depth_name = '서울'
    AND a.region2depth_name = '중구'
    AND a.region3depth_name = '수표동'
    AND p.name = '머거본 알땅콩(135g)'
  ORDER BY i.price ASC
  LIMIT 3;
```

![create_BTree](https://github.com/Painterrr/Lowest-price-daily-necessities-search-service/assets/98957340/2ee8146b-f87c-44ef-8528-cf4e9931bcf2)
- possible_keys: 사용가능한 인덱스
- key: 사용된 인덱스

mart, inventory에서 기본 인덱스만 사용.(5개 중 2개 사용)
심지어 생성한 인덱스가 안 보임

<br>

mysql에서 찾아보면 생성한 인덱스 확인 가능
![create_BTree_in_mysql](https://github.com/Painterrr/Lowest-price-daily-necessities-search-service/assets/98957340/e59366e3-dcaa-474a-90e1-0f9546427817)

mysql workbench에서 EXPLAIN SELECT ~~실행 시 ossible_keys에 PRIMARY,FKqo4nl604cgi0ysa8nmpjnhek5만 확인 가능

이유
- 적합한 인덱스가 아닐 경우
    - 인덱스가 쿼리 조건에 해당하지 않거나, 옵티마이저가 판단하기에 최적의 선택이 아님
- 인덱스가 쿼리에서 사용되지 않을 경우
    - 해당 인덱스를 사용하는 조건이 쿼리의 where 절에 없음
- 인덱스 통계 정보가 최신이 아니어서 무시되는 경우
    - Mysql 옵티마이저는 인덱스 통계 정보를 바탕으로 인덱스 사용 여부 결정
    - 인덱스 통계 정보가 최신이 아닐 경우 인덱스가 무시될 수 있음
- where절에 area, product만 있어서 그런 것 같음

혹시 몰라 통계 정보 최신화
  ```sql
    ANALYZE TABLE inventory;
    ANALYZE TABLE product;
    ANALYZE TABLE mart;
    ANALYZE TABLE area;
  ```

### B+Tree 지정 후

→ 1.563 s
  ```sql
    SELECT i.check_date, i.price, p.name AS product_name, m.name AS mart_name, m.brand AS mart_brand, a.full_addr
    FROM inventory i USE INDEX (idx_inventory_date_product) -- idx_inventory_date_product
    JOIN product p USE INDEX (idx_product_name) ON i.product_id = p.product_id --
    JOIN mart m USE INDEX (idx_mart_name_brand) ON i.mart_id = m.mart_id -- idx_mart_name_brand
    JOIN area a USE INDEX (idx_area_region) ON m.area_id = a.area_id --
    WHERE a.region1depth_name = '서울'
      AND a.region2depth_name = '중구'
      AND a.region3depth_name = '수표동'
      AND p.name = '머거본 알땅콩(135g)'
    ORDER BY i.price ASC
    LIMIT 3;
  ```
![select_BTree](https://github.com/Painterrr/Lowest-price-daily-necessities-search-service/assets/98957340/18ea5699-5820-49be-adf1-94db176e8bd6)


### where 절에 없는 인덱스 삭제

idx_inventory_date_product과 idx_mart_name_brand를 사용하지 않음
해당 인덱스 삭제 후 조회

  ```sql
    -- idx_inventory_date_product, idx_mart_name_brand 삭제
    ALTER TABLE inventory DROP INDEX idx_inventory_date_product;
    ALTER TABLE mart DROP INDEX idx_mart_name_brand;
  ```

  ```sql
    SELECT i.check_date, i.price, p.name AS product_name, m.name AS mart_name, m.brand AS mart_brand, a.full_addr
    FROM inventory i
    JOIN product p USE INDEX (idx_product_name) ON i.product_id = p.product_id
    JOIN mart m ON i.mart_id = m.mart_id
    JOIN area a USE INDEX (idx_area_region) ON m.area_id = a.area_id 
    WHERE a.region1depth_name = '서울'
      AND a.region2depth_name = '중구'
      AND a.region3depth_name = '수표동'
      AND p.name = '머거본 알땅콩(135g)'
    ORDER BY i.price ASC
    LIMIT 3;
  ```

→ 3.937 s

⇒ 조인 where 절에 없어도 join 시 인덱스 사용 확인

## 역량강화
- index 적용 전 조회 속도: 5.067 sec
- index 적용 후 조회 속도: 4.132 sec
- index 지정 후 조회 속도: 1.563 sec
- Explain 시 확인되지 않는(where 절에 없는) 테이블의 인덱스 삭제 시: 3.937 sec
  - Explain에서 확인되지 않는 것일 뿐 join 시 인덱스 테이블 활용
