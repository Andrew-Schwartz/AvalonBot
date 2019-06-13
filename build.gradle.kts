import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.21"

    application
}

repositories {
    jcenter()

    mavenCentral()

    maven(url = "https://kotlin.bintray.com/kotlinx")
    maven(url = "https://gitlab.com/api/v4/projects/10363714/packages/maven")
}

val ktorVersion = "1.2.1"

@Suppress("SpellCheckingInspection")
dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.jessecorbett:diskord:1.4.0")
    implementation("com.jessecorbett:diskord-jvm:1.4.0")

    compile("io.ktor:ktor-client:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
//    compile("io.ktor:ktor-client-apache:$ktorVersion")
    compile("io.ktor:ktor-auth:$ktorVersion")
    compile("io.ktor:ktor-gson:$ktorVersion")

//    testImplementation("org.jetbrains.kotlin:kotlin-test")
//    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile>().all {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs = listOf("-XXLanguage:+InlineClasses")
}

application {
    // Define the main class for the application.
    mainClassName = "avalonBot.MainKt"
}