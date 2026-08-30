import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
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
    implementation(libs.logstash.logback.encoder) {
        exclude(group = "tools.jackson.core")
        exclude(group = "tools.jackson")
    }
    implementation("tools.jackson.core:jackson-databind:3.2.2") // CVE GHSA-72hv-8253-57qq
    implementation(libs.ktor.serialization.jackson)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.klage.kodeverk)
    testImplementation(libs.testcontainers)
    testImplementation(libs.testcontainers.kafka)
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

ktlint {
    version.set(libs.versions.ktlint)
    ignoreFailures.set(false)
    reporters {
        reporter(ReporterType.PLAIN)
        reporter(ReporterType.CHECKSTYLE)
    }
    filter {
        exclude { it.file.path.contains("${File.separator}build${File.separator}") }
    }
}

detekt {
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    buildUponDefaultConfig.set(true)
    ignoreFailures.set(false)
}

// NamedArguments implements RequiresAnalysisApi, so it only reports when detekt
// runs with a compile classpath. The plain `detekt` task has no classpath and
// would silently pass, hence the analysis aware tasks are wired into `check`
// and the plain one is disabled.
tasks.named("detekt") {
    enabled = false
}

tasks.withType<dev.detekt.gradle.Detekt>().configureEach {
    jvmTarget.set(JvmTarget.JVM_21.target)
    reports {
        html.required.set(true)
        checkstyle.required.set(true)
        sarif.required.set(false)
        markdown.required.set(false)
    }
}

tasks.named("check") {
    dependsOn("detektMain", "detektTest")
}

tasks.build {
    dependsOn(tasks.installDist)
}
