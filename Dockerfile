FROM gradle:7.6.1-jdk-alpine AS BUILD_STAGE
COPY --chown=gradle:gradle . /home/gradle
RUN gradle build || return 1

FROM openjdk:17-jdk-alpine
ENV APP_NAME=meetings-bot-1.0.0-SNAPSHOT.jar
ENV APP_HOME=/app
ENV bot_username=$TELEGRAM_USERNAME
ENV bot_token=$TELEGRAM_TOKEN
ENV webhookpath=$TELEGRAM_WEBHOOKPATH
ENV SPRING_PROFILES_ACTIVE=dev,polling
COPY --from=BUILD_STAGE /home/gradle/build/libs/$APP_NAME $APP_HOME/
WORKDIR $APP_HOME
ENTRYPOINT exec java -Dwebhookpath=$TELEGRAM_WEBHOOKPATH -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE -Dbot_username=$TELEGRAM_USERNAME -Dbot_token=$TELEGRAM_TOKEN -jar $APP_NAME
