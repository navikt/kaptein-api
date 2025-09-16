FROM gcr.io/distroless/java21-debian12:nonroot
WORKDIR /app
COPY build/install/kaptein-api/lib /lib
COPY build/resources/main/logback.xml /lib/logback.xml
ENV JAVA_OPTS="-Dlogback.configurationFile=logback.xml"
ENV TZ="Europe/Oslo"
EXPOSE 8080
USER nonroot
ENTRYPOINT ["java", "-cp", "/lib/*", "no.nav.klage.ApplicationKt"]