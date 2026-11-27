plugins {
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
    id("java")
    id("application")
}

group = "ru.azorium.launchserver"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.shell:spring-shell-starter-jansi:4.0.1")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-devtools")

    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

    implementation("org.mariadb.jdbc:mariadb-java-client:2.1.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.apache.commons:commons-compress:1.28.0")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("ru.azorium.launchserver.LauncherApplication")
}

tasks.bootJar {
    archiveFileName.set("LaunchServer.jar")
    mainClass.set("ru.azorium.launchserver.LauncherApplication")
}

tasks.jar {
    enabled = false
}