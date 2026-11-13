FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-25@sha256:add19184d189a1c1ec2a95dc0caa662a8101fe4c4564a4ad7ee42f204e8e9aed
COPY build/install/*/lib /lib
COPY src/main/resources/logback.xml /app/logback.xml
ENV TZ="Europe/Oslo"
EXPOSE 8080
USER nonroot
ENTRYPOINT ["java", "-Dlogback.configurationFile=/app/logback.xml", "-cp", "/app:/lib/*", "no.nav.klage.ApplicationKt"]