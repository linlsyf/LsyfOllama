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
    // ✅ 关键：必须声明这个，否则 bundledPlugin 等扩展函数不可见
    intellijPlatform {
        defaultRepositories()
    }
    // 其他仓库
    maven { url = uri("https://plugins.jetbrains.com/maven") }
    maven { url = uri("https://maven.aliyun.com/repository/public/") }
    google()
    gradlePluginPortal()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    intellijPlatform {
        // ✅ 本地 IDE 实例
        local("D:\\002soft\\ideaIC-2025.2.4.win")

        // ✅ bundledPlugin 必须在 intellijPlatform 块内部
        // Git4Idea 的真实插件 ID 就是 "Git4Idea"（JetBrains 官方表格确认）
        bundledPlugin("Git4Idea")
        bundledPlugin("com.intellij.java")  // Java 插件（可选）

        // 测试框架
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
    }

    // 普通 Maven 依赖写在外面
    implementation("io.github.ollama4j:ollama4j:1.1.4")
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    testCompileOnly("org.projectlombok:lombok:1.18.30")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.30")
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "252"   // ⚠️ 2025.2.4 是 252，不是 251
            untilBuild = "252.*"
        }
        changeNotes = """
            Initial version
        """.trimIndent()
    }
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }
}