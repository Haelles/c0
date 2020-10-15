# gradle 好大
FROM gradle:jdk14
WORKDIR /app
COPY src /app/
RUN gradle build --no-daemon
