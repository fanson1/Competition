import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(project(":app:shared"))
    implementation(compose.desktop.currentOs)
    implementation("org.slf4j:slf4j-simple:2.0.16")
}

compose.desktop {
    application {
        mainClass = "com.example.competition.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg)
            packageName = "Competition"
            packageVersion = "1.0.0"
        }
    }
}
