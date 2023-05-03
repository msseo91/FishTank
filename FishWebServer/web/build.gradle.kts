import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.archivesName

plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("war")
    id("java")
    id("org.jetbrains.kotlin.jvm")
}

group = "com.marineseo.fishtank"
version = "1.0.0"
java.sourceCompatibility = JavaVersion.VERSION_11

tasks.war {
    archiveFileName.set("ROOT.war")
}

dependencies {
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.2")

    // Spring
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-mustache")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    providedRuntime("org.springframework.boot:spring-boot-starter-tomcat")
    runtimeOnly("com.h2database:h2")

    // DB
    runtimeOnly("org.mariadb.jdbc:mariadb-java-client")

    // ETC
    implementation("com.google.code.gson:gson:2.9.0")
    implementation("io.github.java-native:jssc:2.9.4")
    implementation("ch.qos.logback:logback-classic:1.2.11")

    // pi4j
    implementation("com.pi4j:pi4j-core:2.3.0")
    implementation("com.pi4j:pi4j-plugin-raspberrypi:2.3.0")
    implementation("com.pi4j:pi4j-plugin-pigpio:2.3.0")

    // slf4j
    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("org.slf4j:slf4j-simple:1.7.36")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}