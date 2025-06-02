FROM openjdk:17
WORKDIR /app
RUN mvn clean install -DskipTests
COPY target/lucene-owl-search-1.0.0.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]