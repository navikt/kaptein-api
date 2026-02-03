FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-21@sha256:c0d5195f881c9d1464a9581935670d391066c5cbba2d166ae3059c4c20bb623c
COPY build/install/*/lib /lib
COPY src/main/resources/logback.xml /app/logback.xml
ENV TZ="Europe/Oslo"
EXPOSE 8080
USER nonroot
ENTRYPOINT ["java", "-Dlogback.configurationFile=/app/logback.xml", "-cp", "/app:/lib/*", "no.nav.klage.ApplicationKt"]