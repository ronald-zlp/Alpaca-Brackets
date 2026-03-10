import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.intellij") version "1.17.4"
    kotlin("jvm") version "1.9.24"
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test-junit"))
}

intellij {
    type.set(providers.gradleProperty("platformType"))
    version.set(providers.gradleProperty("platformVersion"))
    plugins.set(listOf("java"))
    downloadSources.set(false)
    updateSinceUntilBuild.set(false)
}

kotlin {
    jvmToolchain(17)
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        version.set(providers.gradleProperty("pluginVersion"))
        sinceBuild.set(providers.gradleProperty("sinceBuild"))
        pluginDescription.set(
            """
            <p>Alpaca Brackets gives each nesting level of (), [], and {} its own color so complex code blocks stay readable.</p>
            <p>It skips strings and comments, highlights the active bracket pair around the caret, marks mismatched brackets with precise warnings, and supports smart Java generic plus XML/HTML tag angle brackets.</p>
            """.trimIndent()
        )
        changeNotes.set(
            """
            <p>MVP improvements:</p>
            <ul>
              <li>Rainbow coloring for (), [], and {}</li>
              <li>Active bracket pair highlighting around the caret</li>
              <li>String/comment aware scanning via PSI</li>
              <li>Smart Java generic angle bracket highlighting</li>
              <li>XML/HTML tag angle brackets with nesting-aware scope</li>
              <li>Mismatched bracket highlighting</li>
              <li>Detailed mismatch tooltips</li>
            </ul>
            """.trimIndent()
        )
    }

    buildSearchableOptions {
        enabled = false
    }

    test {
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}
