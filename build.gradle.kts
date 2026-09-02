plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.0.21"
    id("org.jetbrains.intellij.platform") version "2.2.1"
}

group = property("pluginGroup").toString()
version = property("pluginVersion").toString()

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity(property("platformVersion").toString())
        bundledPlugin("Git4Idea")
    }

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testImplementation("org.assertj:assertj-core:3.25.3")
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "21"
    targetCompatibility = "21"
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

intellijPlatform {
    pluginConfiguration {
        id = "com.sambabyte.gitblame"
        name = "sbGitBlame"
        version = property("pluginVersion").toString()
        vendor {
            name = "SambaByte"
            email = "support@sambabyte.com"
            url = "https://github.com/SambaByte"
        }
        ideaVersion {
            sinceBuild = "233"
            untilBuild = "999.*"
        }
    }
}

tasks {
    test {
        useJUnitPlatform()
    }
}
