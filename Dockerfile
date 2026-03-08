FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-21@sha256:bc9cf92fa2a4f1ea4ba6d84ed4b153c00a1c4dec168d2bb0b24b69dabdf216c8
COPY build/install/*/lib /lib
COPY src/main/resources/logback.xml /app/logback.xml
ENV TZ="Europe/Oslo"
EXPOSE 8080
USER nonroot
ENTRYPOINT ["java", "-Dlogback.configurationFile=/app/logback.xml", "-cp", "/app:/lib/*", "no.nav.klage.ApplicationKt"]