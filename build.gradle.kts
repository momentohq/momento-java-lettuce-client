plugins {
    id("java")
    id("com.diffplug.spotless") version "5.15.1"
}

// TODO replace with etsablished group id
group = "momento.lettuce"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.lettuce:lettuce-core:6.4.0.RELEASE")
    implementation("software.momento.java:sdk:1.14.1")

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
