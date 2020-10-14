FROM gradle:jdk14
WORKDIR /app
COPY src /app/
RUN gradle build
