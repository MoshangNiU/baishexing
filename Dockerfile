# ========== 1. Maven 构建阶段 ==========
FROM maven:3.8.8-eclipse-temurin-8 AS builder

WORKDIR /build
COPY pom.xml .

# 使用阿里云 Maven 镜像加速
RUN mkdir -p ~/.m2 && \
    echo '<settings><mirrors><mirror><id>aliyun</id><url>https://maven.aliyun.com/repository/public</url><mirrorOf>central</mirrorOf></mirror></mirrors></settings>' \
    > ~/.m2/settings.xml

RUN mvn dependency:go-offline -B -q
COPY src ./src
RUN mvn package -DskipTests -q

# ========== 2. JRE 运行阶段 ==========
FROM eclipse-temurin:8-jre

WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar

EXPOSE 9527

ENTRYPOINT ["java", "-jar", "app.jar"]
