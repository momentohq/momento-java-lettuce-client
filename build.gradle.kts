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
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
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
