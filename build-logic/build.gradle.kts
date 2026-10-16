plugins {
    `kotlin-dsl`
}

group = "com.jnasser.pokedex.buildlogic"

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.kotlin.compose.gradle.plugin)
    compileOnly(libs.kotlin.serialization.gradle.plugin)
    compileOnly(libs.ksp.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "pokedex.android.application"
            implementationClass = "com.jnasser.pokedex.buildlogic.AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "pokedex.android.library"
            implementationClass = "com.jnasser.pokedex.buildlogic.AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = "pokedex.android.compose"
            implementationClass = "com.jnasser.pokedex.buildlogic.AndroidComposeConventionPlugin"
        }
        register("kotlinJvm") {
            id = "pokedex.kotlin.jvm"
            implementationClass = "com.jnasser.pokedex.buildlogic.KotlinJvmConventionPlugin"
        }
        register("testing") {
            id = "pokedex.testing"
            implementationClass = "com.jnasser.pokedex.buildlogic.TestingConventionPlugin"
        }
        register("retrofit") {
            id = "pokedex.retrofit"
            implementationClass = "com.jnasser.pokedex.buildlogic.RetrofitConventionPlugin"
        }
        register("room") {
            id = "pokedex.room"
            implementationClass = "com.jnasser.pokedex.buildlogic.RoomConventionPlugin"
        }
    }
}
