plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("org.jetbrains.intellij.platform") version "2.5.0"
//    id("org.jetbrains.intellij") version "2.2.0" // 2025.1+ 推荐 ≥1.17.x
}

group = "com.lsyf"
version = "1.0-SNAPSHOT"

repositories {
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
//        create("IU", "2025.1")
//        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)

        local("C:\\\\Program Files\\\\JetBrains\\\\IntelliJ_IDEA") // Windows 路径示例
//
//        localPath.set("C:\\Program Files\\JetBrains\\IntelliJ IDEA 2025.1.4.1") // macOS路径
//        // Windows示例：localPath.set("C:\\Program Files\\JetBrains\\IntelliJ IDEA 2025.1")
//        downloadSources.set(false) // 关闭源码下载（可选）

    }


//    intellij {
////        localPath.set("C:\\Program Files\\JetBrains\\IntelliJ IDEA 2025.1.4.1") // macOS路径
//        // Windows示例：localPath.set("C:\\Program Files\\JetBrains\\IntelliJ IDEA 2025.1")
//        localPath.set("C:\\Program Files\\JetBrains\\IntelliJ_IDEA")
//
//    }
}

//intellijPlatform {
//    pluginConfiguration {
//        ideaVersion {
//            sinceBuild = "251"
//        }
//
//        changeNotes = """
//      Initial version
//    """.trimIndent()
//    }
//}

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
