plugins {
    id("pokedex.kotlin.jvm")
    id("pokedex.retrofit")
    id("pokedex.testing")
}

dependencies {
    implementation(projects.core.model)
    implementation(libs.koin.core)
    implementation(libs.kotlinx.coroutines.core)
}
