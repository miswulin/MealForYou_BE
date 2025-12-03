### 슈멋사 3팀 프로젝트 - [주제] 토핑경제
# 🍱 밀포유 MealForYou - Backend
사용자 개인의 니즈에 맞춘 식재료 밀키트의 온라인 주문 플랫폼입니다.

본 레포지토리는 MealForYou 서비스의 **백엔드(Spring Boot)** 코드와 AWS EC2 + Docker 기반 **배포 및 CI/CD 체계**를 포함합니다.

---
## 🔗 배포 링크
[**MealForYou**](https://www.mealforyou.store)

---

## 📚 Tech Stack

### **Backend**
- Java 17  
- Spring Boot 3  
- Spring Data JPA  
- Spring Security (JWT)  
- MySQL 8  
- Redis  

### **Infra / DevOps**
- AWS EC2 (Amazon Linux 2023)
- GitHub Actions (CI/CD)
- Docker & Docker Compose
- Nginx
- Certbot (SSL/HTTPS)

---

## 📂 Project Structure
```bash
MealForYou
├── Dockerfile
├── build.gradle
├── settings.gradle
├── gradlew
├── gradlew.bat
└── src
    ├── main
    │   ├── java
    │   │   └── store
    │   │       └── mealforyou
    │   │           ├── MealForYouApplication.java   # Spring Boot 메인 클래스
    │   │           ├── config                       # 설정 관련 (Security, Redis, OpenAPI, PortOne 등)
    │   │           ├── constant                     # Enum/상수 (OrderStatus, PaymentType, ProductCategory 등)
    │   │           ├── controller                   # REST API 컨트롤러
    │   │           ├── dto                          # 요청/응답 DTO
    │   │           ├── entity                       # JPA 엔티티 (Member, Dish, Cart, Order 등)
    │   │           ├── exception                    # 글로벌 예외 처리
    │   │           ├── repository                   # Spring Data JPA Repository
    │   │           ├── security                     # 인증/인가, UserDetails
    │   │           │   └── jwt                      # JWT 필터, 토큰 발급/검증
    │   │           ├── service                      # 비즈니스 로직 (주문, 장바구니, 결제, 회원 등)
    │   │           └── util                         # 공통 유틸 
    │   └── resources
    │       ├── application.properties               # 기본 프로필 설정
    │       ├── application-prod.properties          # 운영(prod) 프로필 설정
    │       ├── data.sql                             # 초기 더미 데이터
    │       └── static
    │           └── index.html                       # 간단 헬스체크용 정적 페이지
    │           └── images                           # 제품 이미지 폴더
    └── test
        ├── java
        └── resources
```
---
## 🏛 Domain Overview (주요 도메인)
- Member : 회원 정보, 비밀번호, 연락처, 선호 태그 등
- Dish / Ingredient / DishIngredient : 밀키트 메뉴, 재료 구성, 이미지 등
- Cart / CartItem / CartItemIngredient : 장바구니 및 옵션/재료 선택
- Order / OrderItem / OrderItemIngredient : 주문 및 주문 상세, 재료 기반 수량/금액 계산
- Interest : 회원별 관심/선호 메뉴
- Auth / EmailAuth / RefreshToken : JWT 기반 로그인, 리프레시 토큰, 이메일 인증

---
## 🌐 Architecture Overview
```text
[Client]
↓ HTTPS(443)
[Nginx Reverse Proxy]
↓ Proxy Pass  → 8080
[Spring Boot App Container]
↓
[MySQL / Redis (Docker Compose)]
```
- Nginx가 80/443 포트를 받고, 모든 요청을 8080 Spring Boot 컨테이너로 프록시합니다.
- 데이터베이스(MySQL)와 Redis는 Docker Compose로 함께 관리합니다.

---
## ⚙️ CI/CD 자동 배포 파이프라인

GitHub Actions + EC2 Self-hosted Runner 기반으로, 코드 push 후 백엔드/프론트 자동 빌드 & 배포가 수행됩니다.

### **1️⃣ GitHub Actions 기반 CI/CD 파이프라인 구성**
- 백엔드/프론트엔드 레포지토리에 각각 GitHub Actions Workflow 파일 생성
- main (또는 dev) 브랜치에 push 시, 해당 브랜치에 맞는 워크플로우가 자동 실행
- Workflow는 EC2에 설치된 Self-hosted Runner에서 동작

### **2️⃣ EC2 Self-hosted Runner 환경**
- Amazon Linux 2023 기반 EC2 인스턴스 생성 후 SSH 접속
- GitHub Actions용 Self-hosted Runner 설치 및 systemd 서비스 등록
- 배포를 위한 디렉토리 구조(/home/ec2-user/MealForYou, /var/www/mealforyou) 구성
- Runner가 GitHub Actions Job을 받아 빌드/배포 작업을 실행
---

## 🤖 Backend(Spring Boot) CI/CD 자동화
백엔드 레포지토리(MealForYou_BE)의 Workflow는 다음 순서로 작동합니다.
1. **Trigger**
   - main 브랜치에 push 또는 PR merge 발생 시 백엔드 workflow(be-deploy.yml) 자동 실행
2. **Build**
    - actions/checkout으로 최신 코드 가져오기
    - JDK 17 설치 (Corretto)
    - ./gradlew clean build -x test로 Spring Boot JAR 빌드
3. **Deploy**
    - 빌드된 JAR을 EC2 내부 배포 디렉토리로 복사
    - Dockerfile 기반으로 Spring Boot Docker 이미지 빌드
    - docker-compose up -d --build로 App / MySQL / Redis 컨테이너를 재시작하며 최신 버전 배포

## 🖥 Frontend(React) CI/CD 자동화
프론트 레포지토리(MealForYou_FE)의 Workflow는 다음 순서로 작동합니다.
1. **Trigger**
   - main 혹은 dev 브랜치에 push 시 프론트엔드 workflow(fe-deploy.yml) 자동 실행
2. **Build**
    - Runner에서 Node.js 설정
    - npm ci로 의존성 설치
    - npm run build로 정적 파일(dist) 빌드
3. **Deploy**
    - 빌드 결과물(dist)을 EC2의 /var/www/mealforyou 디렉토리에 복사
    - Nginx가 해당 경로를 정적 파일 루트로 사용하도록 설정
    - 모든 HTTPS(443) 요청 → React 정적 파일 서빙

---

## 🔒 HTTPS / SSL
- Certbot을 통해 Let’s Encrypt 인증서를 자동 발급
- 모든 HTTP 요청은 HTTPS로 리다이렉트
- 인증서는 cron + certbot으로 자동 갱신

---
## 👥 Backend Contributors
- 서울여대 멋사 13기 소프트웨어융합학과 24학번 우예빈
- 서울여대 멋사 13기 소프트웨어융합학과 23학번 박채린
- 서울여대 멋사 13기 디지털미디어학과 22학번 이다겸
 
