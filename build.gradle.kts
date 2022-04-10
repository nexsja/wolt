import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jmailen.gradle.kotlinter.tasks.FormatTask
import org.jmailen.gradle.kotlinter.tasks.LintTask
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    val kotlinVersion = "1.6.10"

    id("org.springframework.boot") version "2.6.4"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion

    // Code Quality
    id("io.gitlab.arturbosch.detekt") version "1.19.0"
    id("org.jmailen.kotlinter") version "3.8.0"
}

group = "lv.alexn"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}

dependencies {

    implementation(springBoot("web"))
    implementation(springBoot("validation"))

    testImplementation(springBoot("test"))
}

fun springBoot(module: String) = "org.springframework.boot:spring-boot-starter-$module"

detekt {
    config = files("detekt.yml")
    buildUponDefaultConfig = true
}

tasks {

    withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "11"
        }
    }

    // disable plain.jar
    getByName<Jar>("jar") {
        enabled = false
    }

    withType<Test> {
        useJUnitPlatform()
    }

    withType<Detekt>().configureEach {
        reports {
            xml.required.set(false)
            txt.required.set(false)
            // Jenkins pipeline can only publish index.html
            html.required.set(true)
            html.outputLocation.set(file("build/reports/detekt/index.html"))
        }
    }

    withType<LintTask> {
        source = fileTree("src")
    }

    withType<FormatTask> {
        source = fileTree("src")
    }

    withType<BootJar> {
        archiveBaseName.set("app")
    }

    val test by existing(Test::class) {
        useJUnitPlatform {
            excludeTags(IntegrationTest.integrationTestTag)
        }
    }

    val itest by registering(IntegrationTest::class) {
        shouldRunAfter(test)
        useJUnitPlatform {
            includeTags(IntegrationTest.integrationTestTag)
        }
    }

    check {
        dependsOn(itest)
    }
}

open class IntegrationTest : Test() {

    companion object Tag {
        const val integrationTestTag = "itest"
    }

    @Internal override fun getGroup(): String = JavaBasePlugin.VERIFICATION_GROUP
    @Internal override fun getDescription(): String = "Runs the integration tests."
}

