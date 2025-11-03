# Sử dụng image JDK 17 nhẹ
FROM openjdk:17-jdk-slim

# Đặt thư mục làm việc trong container
WORKDIR /app

# Copy toàn bộ mã nguồn vào container
COPY . .

# Build project bằng Maven Wrapper (đã có sẵn trong repo)
RUN mvn clean package -DskipTests 

# Chạy file JAR sinh ra trong target/
CMD ["java", "-jar", "target/yte-gemini-doctor-0.0.1-SNAPSHOT.jar"]
