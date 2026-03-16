import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import com.vanniktech.maven.publish.SonatypeHost
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.JavadocJar

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.vanniktech.publish)
}

kotlin {
    jvmToolchain(17)

    // Android
    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_17)
                }
            }
        }
        publishLibraryVariants("release")
    }

    // JVM Desktop
    jvm()

    // iOS
    listOf(iosArm64(), iosSimulatorArm64(), iosX64()).forEach { target ->
        target.binaries.framework {
            baseName = "WacDiscovery"
            isStatic = true
        }
    }

    sourceSets {
        all {
            languageSettings.optIn("kotlin.ExperimentalMultiplatform")
        }
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        val jvmMain by getting {
            dependencies {
                implementation(libs.jmdns)
            }
        }
    }
}

android {
    namespace = "com.wac.wacdiscovery"
    compileSdk = 35

    defaultConfig {
        minSdk = 21
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

mavenPublishing {
    configure(KotlinMultiplatform(javadocJar = JavadocJar.Empty()))

    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    pom {
        name.set("WAC Discovery")
        description.set("Kotlin Multiplatform library for network device discovery (SSDP + mDNS)")
        url.set("https://github.com/WacLabs/wac-discovery")
        inceptionYear.set("2025")

        licenses {
            license {
                name.set("Apache License 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0")
            }
        }

        developers {
            developer {
                id.set("WacLabs")
                name.set("WacLabs")
                email.set("quanmtfu@gmail.com")
            }
        }

        scm {
            url.set("https://github.com/WacLabs/wac-discovery")
            connection.set("scm:git:git://github.com/WacLabs/wac-discovery.git")
            developerConnection.set("scm:git:ssh://github.com/WacLabs/wac-discovery.git")
        }
    }
}
