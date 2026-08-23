plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("org.jetbrains.intellij.platform") version "2.5.0"
    id("distribution")
    id("io.freefair.lombok") version "8.6"
}

group = "com.lsyf"
version = "0.0.2"

// ✅ 关键修复：强制指定 JDK 21（IntelliJ Platform Plugin 2.x 必须）
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

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
    // 关键：注册 JetBrains 的 IDE 仓库，Verifier 才能下载对应 IDE 构建

}

dependencies {
    intellijPlatform {
        local("D:\\002soft\\idea-2026.1.2.win")

        bundledPlugin("Git4Idea")
        bundledPlugin("com.intellij.java")
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
    }

    implementation("io.github.ollama4j:ollama4j:1.1.4")
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "252"
            untilBuild = "261.*"
        }
        changeNotes = "Initial version"
    }
}

tasks.buildPlugin {
    archiveFileName.set("LsyfOllama-${version}.zip")
}