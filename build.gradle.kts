plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("org.jetbrains.intellij.platform") version "2.5.0"
    id("distribution")// 引入分发插件
}

group = "com.lsyf"
version = "1.0-SNAPSHOT"

repositories {
    maven { url = uri("https://plugins.jetbrains.com/maven") }
    maven { url = uri("https://maven.aliyun.com/repository/public/") }
    maven { url = uri("https://maven.aliyun.com/repository/google/") }
    maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin/") }
    maven { url = uri("https://maven.aliyun.com/repository/central/") }
    // 默认仓库
    google()
    maven { url = uri("https://jitpack.io") } // 其他自定义仓库
    // 默认仓库（保持兼容性）
    gradlePluginPortal()
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
dependencies {
    intellijPlatform {

        local("C:\\Program Files\\JetBrains\\IntelliJ IDEA Community Edition 2025.1.3") // Windows 路径示例
//        local("C:\\\\Program Files\\\\JetBrains\\\\IntelliJ_IDEA") // Windows 路径示例


    }
    implementation("io.github.ollama4j:ollama4j:1.0.100")
}


tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }
}
