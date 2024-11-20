import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.21"
}

group = "org.sereinfish.catcat.framework.eventhandler.extend"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("org.sereinfish.catcat.frame:CatFrame:0.0.206")
    implementation("org.slf4j:slf4j-api:2.0.12")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes["CatPluginId"] = "org.sereinfish.catcat.framework.eventhandler.extend"
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}