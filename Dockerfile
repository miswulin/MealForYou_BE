FROM amazoncorretto:17-alpine

# 컨테이너 내부 작업 디렉토리
WORKDIR /app

# 빌드된 실행용 JAR을 컨테이너 내부로 복사
COPY build/libs/*.jar app.jar

# 컨테이너 시작 시 실행할 명령
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]