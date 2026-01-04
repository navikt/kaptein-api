plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
}

group = "no.nav.klage"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.server.websockets)
    implementation(libs.ktor.server.openapi)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.compression)
    implementation(libs.smiley4.openapi.generator)
    implementation(libs.smiley4.swagger.ui)
    implementation(libs.smiley4.schema.kenerator.core)
    implementation(libs.smiley4.schema.kenerator.reflection)
    implementation(libs.smiley4.schema.kenerator.swagger)
    implementation(libs.logback.classic)
    implementation(libs.ktor.server.config.yaml)
    implementation(libs.kafka.clients)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.jackson.datatype.jsr310)
    implementation(libs.logstash.logback.encoder)
    implementation(libs.ktor.serialization.jackson)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.klage.kodeverk)
    testImplementation(libs.testcontainers)
    testImplementation(libs.testcontainers.kafka)
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.build {
    dependsOn(tasks.installDist)
}