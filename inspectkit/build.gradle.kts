plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

group = (findProperty("GROUP") as String?) ?: "dev.inspectkit"
version = (findProperty("VERSION_NAME") as String?) ?: "0.0.0"

android {
    namespace = "dev.inspectkit"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
        consumerProguardFiles("consumer-rules.pro")
        manifestPlaceholders["inspectkitLauncherEnabled"] = "false"
    }

    buildTypes {
        debug {
            manifestPlaceholders["inspectkitLauncherEnabled"] = "true"
        }
        release {
            manifestPlaceholders["inspectkitLauncherEnabled"] = "false"
        }
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = project.group.toString()
                artifactId = "inspectkit"
                version = project.version.toString()

                pom {
                    name.set("InspectKit")
                    description.set("Drop-in in-app inspection tools for Android debug builds.")
                }
            }
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)

    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.sqlite:sqlite:2.4.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}
