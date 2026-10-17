FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY . .
RUN javac VehicleManager.java RydexServer.java
EXPOSE 8080
CMD ["java", "RydexServer"]
