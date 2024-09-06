plugins {
    id("java")
    id("com.diffplug.spotless") version "5.15.1"

    // Publishing plugins
    id("maven-publish")
    id("signing")
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
}

group = "software.momento.java"
version = "0.1.0"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17

    // For publishing
    withSourcesJar()
    withJavadocJar()
}

dependencies {
    implementation("io.lettuce:lettuce-core:6.4.0.RELEASE")
    implementation("software.momento.java:sdk:1.15.0")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.slf4j:slf4j-api:2.0.16")
    testImplementation("ch.qos.logback:logback-classic:1.5.7")
}

tasks.test {
    useJUnitPlatform()
    outputs.upToDateWhen { false }
    testLogging {
        events("PASSED", "FAILED", "SKIPPED")
        showStandardStreams = true
    }
}

spotless {
    java {
        removeUnusedImports()
        googleJavaFormat("1.11.0")
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = group.toString()
            artifactId = "momento-lettuce"
            version = project.version.toString()

            pom {
                name.set("Momento Lettuce Compatibility Client")
                description.set("Momento-backed implementation of the Lettuce Redis client")
                url.set("https://github.com/momentohq/momento-java-lettuce-client")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("momento")
                        name.set("Momento")
                        organization.set("Momento")
                        email.set("eng-deveco@momentohq.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/momentohq/momento-java-lettuce-client.git")
                    developerConnection.set("scm:git:git@github.com:momentohq/momento-java-lettuce-client.git")
                    url.set("https://github.com/momentohq/momento-java-lettuce-client")
                }
            }
        }
    }
}

// Signing and Nexus publishing setup
val signingKey: String? = System.getenv("SONATYPE_SIGNING_KEY")
val signingPassword: String? = System.getenv("SONATYPE_SIGNING_KEY_PASSWORD")

if (signingKey != null && signingPassword != null) {
    signing {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications["mavenJava"])
    }
}

val sonatypeUsername: String? = System.getenv("SONATYPE_USERNAME")
val sonatypePassword: String? = System.getenv("SONATYPE_PASSWORD")

if (sonatypeUsername != null && sonatypePassword != null) {
    nexusPublishing {
        repositories {
            sonatype {
                nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
                snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
                username.set(sonatypeUsername)
                password.set(sonatypePassword)
            }
        }
    }
}
