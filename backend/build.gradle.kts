plugins {
    kotlin("jvm") version "2.0.0"
    application
    // 如需 @Serializable 再開啟：
    // kotlin("plugin.serialization") version "2.0.0"
}

// 注意：不要在這裡寫 repositories{}，我們用 settings.gradle.kts 管理

dependencies {
    val ktor = "2.3.12"

    implementation("io.ktor:ktor-server-core-jvm:$ktor")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktor")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor")
    implementation("io.ktor:ktor-server-cors-jvm:$ktor")

    testImplementation(kotlin("test"))
    // 如果開啟了 serialization plugin，再加這行：
    // implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
}

application {
    // 對應 src/main/kotlin/com/example/Application.kt
    mainClass.set("com.example.ApplicationKt")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "21"
}
