plugins {
    `kotlin-dsl`
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

repositories {
    google()
    mavenCentral()
    maven(url = "https://jitpack.io")
    gradlePluginPortal()
}

dependencies {
    implementation(libs.android.gradle.plugin)
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.compose.compiler.plugin)
    implementation(libs.hilt.gradle.plugin)
    implementation(libs.detekt.gradle.plugin)
    implementation(libs.kotlinx.serialization.plugin)
    implementation(libs.ksp.plugin)
    implementation(libs.cashapp.molecule.plugin)
    implementation(libs.room.plugin)
    implementation(libs.paparazzi.plugin)
    implementation(libs.module.graph.plugin)

    // Make version catalog available in precompiled scripts
    // https://github.com/gradle/gradle/issues/15383#issuecomment-1567461389
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}
