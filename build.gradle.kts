import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.40"
    java

    application
}

repositories {
    jcenter()
    mavenCentral()

//    maven(url = "https://kotlin.bintray.com/kotlinx")
//    maven(url = "https://gitlab.com/api/v4/projects/10363714/packages/maven")
}

val ktorVersion = "1.2.1"

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("io.ktor:ktor-client:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-json:$ktorVersion")
    implementation("io.ktor:ktor-auth:$ktorVersion")
    implementation("io.ktor:ktor-gson:$ktorVersion")

//    implementation("net.dv8tion:JDA:3.8.3_463") {
//        exclude(module = "opus-java")
//    }
//    implementation("com.jessecorbett:diskord-jvm:1.4.0")

//    testImplementation("org.jetbrains.kotlin:kotlin-test")
//    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks.jar {
    manifest {
        attributes("Main-Class" to "avalonBot.MainKt")
    }
    from({ configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) } })
}

tasks.withType<KotlinCompile>().all {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs = listOf(
            "-XXLanguage:+InlineClasses",
            "-Xuse-experimental=kotlin.ExperimentalUnsignedTypes",
            "-Xallow-result-return-type"
    )
}

application {
    // Define the main class for the application.
    mainClassName = "avalonBot.MainKt"
}