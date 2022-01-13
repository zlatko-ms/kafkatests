FROM openjdk:11

ARG JAR_FILE=*.jar
ARG CONF_FILE=configuration.properties
ARG START_APP_FILE=startapp.sh
ARG HOME_DIR=/usr/share/app

RUN mkdir -p /usr/share/app
COPY ${JAR_FILE} /usr/share/app/app.jar
COPY ${CONF_FILE} /usr/share/app/app.properties
COPY ${START_APP_FILE} /usr/share/app/startapp.sh
RUN chmod +x /usr/share/app/startapp.sh
RUN apt-get update && apt-get -y install

WORKDIR /usr/share/app
ENTRYPOINT ["/usr/share/app/startapp.sh"]

#RUN javac Main.java
#CMD ["java", "Main"]
