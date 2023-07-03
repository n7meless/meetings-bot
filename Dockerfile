FROM openjdk:17-alpine

ENV APP_FILE meetings-bot-1.0.0-SNAPSHOT.jar
ENV APP_HOME /app
EXPOSE 8080
COPY build/libs/$APP_FILE $APP_HOME/meetings-bot-1.0.0.jar
WORKDIR $APP_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["exec java -jar meetings-bot-1.0.0.jar"]
#ENTRYPOINT ["java","-jar","$APP_HOME/$APP_FILE"]