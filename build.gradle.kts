import org.gradle.api.JavaVersion.VERSION_21

plugins {
    `java-library`
    `jacoco`
    id("io.freefair.lombok") version "8.12"
}

java {
    sourceCompatibility = VERSION_21
    targetCompatibility = VERSION_21
}

repositories {
    mavenCentral()
}

dependencies {
    api("com.fasterxml.jackson.core:jackson-databind:2.18.2")

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
