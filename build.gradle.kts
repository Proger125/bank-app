plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("ch.qos.logback:logback-core:1.5.0")
    implementation("org.slf4j:slf4j-api:2.0.12")
    implementation("com.mysql:mysql-connector-j:8.3.0")


    testImplementation("ch.qos.logback:logback-classic:1.5.0")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}