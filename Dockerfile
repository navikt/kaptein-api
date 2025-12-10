FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-21@sha256:2ab80d349d0f4a476f444ed7e2ca0f5080f74cf5e5dac7ed25dd4ea36a40f612
COPY build/install/*/lib /lib
COPY src/main/resources/logback.xml /app/logback.xml
ENV TZ="Europe/Oslo"
EXPOSE 8080
USER nonroot
ENTRYPOINT ["java", "-Dlogback.configurationFile=/app/logback.xml", "-cp", "/app:/lib/*", "no.nav.klage.ApplicationKt"]