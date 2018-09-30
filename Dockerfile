FROM gradle:jdk8-alpine as builder
COPY --chown=gradle:gradle . /home/gradle
WORKDIR /home/gradle
RUN gradle jar

FROM openjdk:8-alpine
COPY --from=builder /home/gradle/build/libs/newporn-bot-*.jar /app/newporn-bot.jar
WORKDIR /app
CMD java -jar newporn-bot.jar
