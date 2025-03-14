# Stage 1: Cache Gradle dependencies
FROM gradle:8.12.1-jdk21 AS cache
RUN mkdir -p /home/gradle/cache_home
ENV GRADLE_USER_HOME=/home/gradle/cache_home
COPY build.gradle.kts gradle.properties settings.gradle.kts /home/gradle/app/
#COPY composeApp/build.gradle.* /home/gradle/app/composeApp
COPY server/build.gradle.* /home/gradle/app/server
COPY shared/build.gradle.* /home/gradle/app/shared
COPY gradle /home/gradle/app/gradle
WORKDIR /home/gradle/app
RUN gradle build -i --stacktrace

# Stage 2: Build Application
FROM gradle:8.12.1-jdk21 AS build
COPY --from=cache /home/gradle/cache_home /home/gradle/.gradle
COPY . /usr/src/app/
WORKDIR /usr/src/app
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
# Build the fat JAR
RUN gradle server:buildFatJar --no-daemon

# Stage 3: Create the Runtime Image
FROM amazoncorretto:21 AS runtime
EXPOSE 8070

RUN mkdir /app

COPY --from=build /home/gradle/src/server/build/libs/*.jar /app/bitauth-server.jar
ENTRYPOINT ["java","-server", "-XX:+UseContainerSupport", "-XX:InitialRAMFraction=2", "-XX:MinRAMFraction=2", "-XX:MaxRAMFraction=2", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=100", "-XX:+UseStringDeduplication", "-jar", "-Djava.security.egd=file:/dev/./urandom","-jar","/app/bitauth-server.jar"]