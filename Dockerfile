FROM gradle:7.6.1-jdk-alpine AS build
WORKDIR /usr/app/
COPY . .
RUN gradle build --no-daemon --scan --stacktrace

FROM openjdk:17-jdk-alpine
ENV APP_FILE=meetings-bot-1.0.0-SNAPSHOT.jar
ENV APP_HOME=/usr/app/
ENV bot_username=$TELEGRAM_USERNAME
ENV bot_token=$TELEGRAM_TOKEN
ENV SPRING_PROFILES_ACTIVE=dev,polling
WORKDIR $APP_HOME
COPY --from=BUILD $APP_HOME .
EXPOSE 8080
ENTRYPOINT java -jar -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE -Dbot_username=$TELEGRAM_USERNAME -Dbot_token=$TELEGRAM_TOKEN $APP_FILE
