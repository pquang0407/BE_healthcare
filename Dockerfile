FROM openjdk:17-jdk-slim

# Cài Maven
RUN apt-get update && apt-get install -y maven

WORKDIR /app
COPY . .

RUN mvn clean package -DskipTests

CMD ["java", "-jar", "target/yte-gemini-doctor-0.0.1-SNAPSHOT.jar"]
