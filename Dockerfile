FROM openjdk:12
WORKDIR /app
COPY src /app/
RUN mkdir -p dist
RUN javac -cp src -d dist
