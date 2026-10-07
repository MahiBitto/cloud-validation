FROM eclipse-temurin:17-jre

#RUN groupadd --system --gid 10001 keymaker \
#    && useradd --system --uid 10001 --gid 10001 --no-create-home keymaker

WORKDIR /app

#COPY --chown=keymaker:keymaker target/keymaker-service-0.0.1-SNAPSHOT.jar app.jar

#USER 10001:10001

COPY --chown=65534:65534 target/keymaker-service-0.0.1-SNAPSHOT.jar app.jar

USER 65534:65534

EXPOSE 8071

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
