# 1단계: 빌드 (Maven + Java 17)
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

# pom.xml 먼저 복사하여 의존성 캐싱
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 소스 복사 및 빌드 (테스트 생략)
COPY src ./src
RUN mvn clean package -DskipTests -B

# 2단계: 실행 (Java 17 slim)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 빌드된 JAR 복사
COPY --from=build /app/target/*.war app.war

# 업로드 파일 저장 디렉토리 생성
RUN mkdir -p /root/uploadFiles

# UTF-8 한글 환경
ENV LANG=ko_KR.UTF-8
ENV TZ=Asia/Seoul

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.war"]
