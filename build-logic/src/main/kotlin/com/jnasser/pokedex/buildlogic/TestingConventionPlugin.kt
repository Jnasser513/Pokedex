package com.jnasser.pokedex.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class TestingConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        dependencies {
            add("testImplementation", libs.library("junit4"))
            add("testImplementation", libs.library("kotlinx-coroutines-test"))
            add("testImplementation", libs.library("turbine"))
        }

        pluginManager.withPlugin("com.android.application") {
            dependencies {
                add("androidTestImplementation", platform(libs.library("androidx-compose-bom")))
                add("androidTestImplementation", libs.library("androidx-junit"))
                add("androidTestImplementation", libs.library("androidx-espresso-core"))
                add("androidTestImplementation", libs.library("androidx-compose-ui-test-junit4"))
            }
        }
        pluginManager.withPlugin("com.android.library") {
            dependencies {
                add("androidTestImplementation", libs.library("androidx-junit"))
                add("androidTestImplementation", libs.library("androidx-espresso-core"))
            }
        }
    }
}
