# ---- Build stage ----
FROM eclipse-temurin:21-jdk AS build
# 直接在 backend 子資料夾工作
WORKDIR /workspace/backend
# 只複製 backend 目錄內容（包含 gradlew、settings.gradle.kts、src/...）
COPY backend/ .
# 讓 wrapper 可執行
RUN chmod +x ./gradlew
# 預熱相依，增加快取命中率
RUN ./gradlew --no-daemon --version
# 產生可執行發行版（bin/ + lib/）
RUN ./gradlew --no-daemon --stacktrace --info clean installDist

# ---- Run stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app
# 從 backend 的 build 產物複製
COPY --from=build /workspace/backend/build/install/backend /app
ENV PORT=8080
EXPOSE 8080
CMD ["/app/bin/backend"]
