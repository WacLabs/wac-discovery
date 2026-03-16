plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

dependencies {
    implementation(project(":wac-discovery"))
    implementation(libs.kotlinx.coroutines.core)
}

application {
    mainClass.set("com.wac.wacdiscovery.example.MainKt")
}

kotlin {
    jvmToolchain(17)
}
