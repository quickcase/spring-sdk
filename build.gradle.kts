import org.gradle.api.JavaVersion.VERSION_21

group = "app.quickcase"
version = "0.8.1"

plugins {
    `java-library`
    `jacoco`
    `maven-publish`
    id("io.freefair.lombok") version "9.2.0"
    id("io.spring.dependency-management") version "1.1.7"
}

java {
    sourceCompatibility = VERSION_21
    targetCompatibility = VERSION_21
}

repositories {
    mavenCentral()
}

publishing {
    publications {
        create<MavenPublication>("springSdk") {
            from(components["java"])

            pom {
                url.set("https://github.com/quickcase/spring-sdk.git")
            }

            // Capture dependency versions resolved from Spring Boot BOM via dependency management
            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime"){
                    fromResolutionResult()
                }
            }

            repositories {
                maven {
                    name = "github"
                    url = uri("https://maven.pkg.github.com/quickcase/spring-sdk")
                    credentials {
                        username = project.findProperty("github.user") as String? ?: System.getenv("GH_USERNAME")
                        password = project.findProperty("github.key") as String? ?: System.getenv("GH_TOKEN")
                    }
                }
            }
        }
    }
}

val versions = mapOf(
    "springBoot" to "3.5.9"
)

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:${versions["springBoot"]}")
    }
}

dependencies {
    api("com.fasterxml.jackson.core:jackson-databind")

    api("org.springframework.boot:spring-boot-starter-oauth2-client")
    api("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    api("org.springframework.boot:spring-boot-starter-security")
    api("org.springframework.boot:spring-boot-starter-web")

    implementation("ch.qos.logback:logback-classic")

    testImplementation("org.hamcrest:hamcrest:3.0")
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.mockito:mockito-junit-jupiter")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter("5.11.4")
        }
    }
}

tasks.withType(JavaCompile::class).configureEach {
    // Explicitly required since Spring 6.1 for mapping of application properties via constructor binding
    // https://github.com/spring-projects/spring-framework/wiki/Upgrading-to-Spring-Framework-6.x#parameter-name-retention
    options.compilerArgs.add("-parameters")
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)

    reports {
        html.required.set(true)
    }

    // Exclusions from reports, for verification exclusion see task `jacocoTestCoverageVerification`
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                exclude(
                    "**/*Configuration.class", // Spring configuration classes
                    "app/quickcase/sdk/spring/auth/QuickcaseOAuth2ResourceServerCustomizer.class" // Spring customizer
                )
            }
        })
    )
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.9".toBigDecimal()
            }

            // Exclusions from verification
            classDirectories.setFrom(
                files(classDirectories.files.map {
                    fileTree(it) {
                        exclude(
                            "**/*Configuration.class", // Spring auto-config classes
                            "app/quickcase/sdk/spring/auth/QuickcaseOAuth2ResourceServerCustomizer.class" // Spring customizer
                        )
                    }
                })
            )
        }
    }
}
