plugins {
    id("java")
    id("com.diffplug.spotless") version "5.15.1"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.lettuce:lettuce-core:6.4.0.RELEASE")
    implementation("software.momento.java:momento-lettuce:0.1.0")
    implementation("software.momento.java:sdk:1.15.0")
}

spotless {
    java {
        removeUnusedImports()
        googleJavaFormat("1.11.0")
    }
}