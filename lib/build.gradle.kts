import org.gradle.api.JavaVersion.VERSION_17

plugins {
    `java-library`
    `jacoco`
}

java {
    sourceCompatibility = VERSION_17
    targetCompatibility = VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    api("com.fasterxml.jackson.core:jackson-databind:2.15.1")

    testImplementation("org.hamcrest:hamcrest:2.2")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter("5.9.1")
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
