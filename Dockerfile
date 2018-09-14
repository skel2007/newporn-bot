FROM openjdk:8-alpine
COPY build/libs/newporn-bot-*.jar /newporn-bot.jar
CMD java -jar newporn-bot.jar
