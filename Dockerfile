# gradle 好大
FROM gradle:jdk14
WORKDIR /app
COPY src build.gradle settings.gradle .project miniplc0-java.iml /app/
RUN gradle fatjar --no-daemon
