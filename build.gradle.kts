plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("org.jetbrains.intellij.platform") version "2.5.0"
    id("distribution")
    id("io.freefair.lombok") version "8.6"
}

group = "com.lsyf"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
    maven { url = uri("https://plugins.jetbrains.com/maven") }
    maven { url = uri("https://maven.aliyun.com/repository/public/") }
    google()
    gradlePluginPortal()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    intellijPlatform {
        local("D:\\002soft\\ideaIC-2025.2.4.win")

        bundledPlugin("Git4Idea")
        bundledPlugin("com.intellij.java")

        // ✅ 关键：添加 Lombok 插件（运行时依赖）
//        plugin("org.projectlombok.lombok-plugin:1.18.30")

        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
    }

    implementation("io.github.ollama4j:ollama4j:1.1.4")


}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "252"
            untilBuild = "252.*"
        }
        changeNotes = "Initial version"
    }
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
        // ✅ freefair 会自动设置 annotationProcessorPath
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }
}