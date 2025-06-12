import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins { // 스프링 부트의 의존성을 관리해주는 Plugin
    id("org.springframework.boot") version "2.6.7"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("org.jlleitschuh.gradle.ktlint") version "10.2.0"
    id("org.jlleitschuh.gradle.ktlint-idea") version "10.2.0"
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.spring") version "1.6.21" // wrapped all-open
    kotlin("plugin.jpa") version "1.3.61" // wrapped no-arg
}

allOpen {
    annotation("javax.persistence.Entity") // @Entity가 붙은 클래스에 한해서만 all open 플러그인을 적용
}

noArg {
    annotation("javax.persistence.Entity") // @Entity가 붙은 클래스에 한해서만 no arg 플러그인을 적용
}

group = "com.codeUnicorn"
version = "0.0.1"
java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories { // 라이브러리들을 어떤 원격 저장소에서 받을 지 선택
    mavenCentral()
}

dependencies { // 프로젝트에 필요한 라이브러리 관리
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    // 세션 데이터를 DB에 저장하기 위해 spring-session-jdbc 사용
    implementation("org.springframework.session:spring-session-jdbc")
    // Logging
    implementation("io.github.microutils:kotlin-logging-jvm:2.1.20")
    implementation("org.bgee.log4jdbc-log4j2:log4jdbc-log4j2-jdbc4.1:1.16")
    implementation("org.codehaus.janino:janino")
    implementation("org.springframework.cloud:spring-cloud-starter-aws:2.2.6.RELEASE")
    implementation("com.github.maricn:logback-slack-appender:1.4.0")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.9.9")

    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("mysql:mysql-connector-java") // MySQL

    // assertj
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// plain.jar 파일 빌드 안되게 하기
tasks.getByName<Jar>("jar") {
    enabled = false
}
