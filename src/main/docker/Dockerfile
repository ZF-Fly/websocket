FROM amazoncorretto:latest
ENV APPLICATION='wsxch-restapi'
VOLUME /tmp
ADD wsxch.restapi-3.2.0.jar wsxch.jar
RUN bash -c 'touch /wsxch.jar'
RUN ln -sf /usr/share/zoneinfo/America/New_York /etc/localtime
RUN echo 'America/New_York' >/etc/timezone
EXPOSE 8407
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "-XX:+UseContainerSupport","-XX:MaxRAMPercentage=70.0", "/wsxch.jar"]
