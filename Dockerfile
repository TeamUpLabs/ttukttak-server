# CI에서 ./gradlew bootJar로 만든 jar를 담아요.
# RUN이 없어서 에뮬레이션 없이 amd64·arm64 이미지를 함께 만들어요.
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY build/libs/app.jar app.jar
USER 10001:10001
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
