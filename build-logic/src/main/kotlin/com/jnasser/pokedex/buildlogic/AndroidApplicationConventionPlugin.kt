package com.jnasser.pokedex.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.application")

        extensions.configure<ApplicationExtension> {
            namespace = "com.jnasser.pokedex"
            compileSdk {
                version = release(libs.version("compileSdk")) {
                    minorApiLevel = libs.version("compileSdkMinor")
                }
            }

            defaultConfig {
                applicationId = "com.jnasser.pokedex"
                minSdk = libs.version("minSdk")
                targetSdk = libs.version("targetSdk")
                versionCode = 1
                versionName = "1.0"
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }

            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }

            buildTypes {
                release {
                    isMinifyEnabled = false
                }
            }
        }
    }
}
