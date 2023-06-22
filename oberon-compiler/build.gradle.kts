plugins {
    java
    id("antlr")
    id("org.springframework.boot") version "3.1.0"
    id("io.spring.dependency-management") version "1.1.0"
}

java.sourceCompatibility = JavaVersion.VERSION_17


repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.antlr:antlr4:4.12.0")

    implementation("org.bytedeco:javacpp:1.5.9")
    implementation("org.bytedeco:llvm-platform:16.0.4-1.5.9")


    annotationProcessor("org.projectlombok:lombok")
    compileOnly("org.projectlombok:lombok")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude("junit", "junit")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

