plugins {
    kotlin("jvm") version "1.9.0"
    kotlin("plugin.serialization") version "1.9.0" // Apply the serialization plugin
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { url = uri("https://maven.waltid.dev/releases") }

}

dependencies {
    testImplementation(kotlin("test"))
    implementation("id.walt.credentials:waltid-verifiable-credentials:0.7.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1") // Add serialization dependency

}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}

application {
    mainClass.set("MainKt")
}