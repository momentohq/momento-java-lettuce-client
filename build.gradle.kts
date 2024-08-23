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
    testImplementation("org.slf4j:slf4j-api:2.1.0-alpha1")
    testImplementation("ch.qos.logback:logback:0.5")
}

tasks.test {
    useJUnitPlatform()
}

spotless {
    java {
        removeUnusedImports()
        googleJavaFormat("1.11.0")
    }
}
