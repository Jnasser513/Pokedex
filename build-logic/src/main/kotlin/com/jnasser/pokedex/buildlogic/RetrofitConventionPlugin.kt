package com.jnasser.pokedex.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class RetrofitConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")

        dependencies {
            add("implementation", libs.bundle("retrofit"))
            add("testImplementation", libs.library("okhttp-mockwebserver"))
        }
    }
}
