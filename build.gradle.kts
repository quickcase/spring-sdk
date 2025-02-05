import org.gradle.api.JavaVersion.VERSION_21

plugins {
    `java-library`
    `jacoco`
    id("io.freefair.lombok") version "8.12.1"
    id("io.spring.dependency-management") version "1.1.7"
}

java {
    sourceCompatibility = VERSION_21
    targetCompatibility = VERSION_21
}

repositories {
    mavenCentral()
}

val versions = mapOf(
    "springBoot" to "3.4.2"
)

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:${versions["springBoot"]}")
    }
}

dependencies {
    api("com.fasterxml.jackson.core:jackson-databind")
    api("org.springframework.boot:spring-boot-starter-web")

    implementation("ch.qos.logback:logback-classic")

    testImplementation("org.hamcrest:hamcrest:3.0")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter("5.11.4")
        }
    }
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)

    reports {
        html.required.set(true)
    }
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.9".toBigDecimal()
            }
        }
    }
}
