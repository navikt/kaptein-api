FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-21@sha256:2b210f8ade2034664fb6c7a38f503f84e8bfd1d5f9be7fdab7a63ec80dcf3780
COPY build/install/*/lib /lib
COPY src/main/resources/logback.xml /app/logback.xml
ENV TZ="Europe/Oslo"
EXPOSE 8080
USER nonroot
ENTRYPOINT ["java", "-Dlogback.configurationFile=/app/logback.xml", "-cp", "/app:/lib/*", "no.nav.klage.ApplicationKt"]