plugins {
    id("pokedex.android.application")
    id("pokedex.android.compose")
    id("pokedex.testing")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.database)
    implementation(projects.core.designsystem)
    implementation(projects.core.model)
    implementation(projects.core.domain)
    implementation(projects.core.network)
    implementation(projects.feature.favorites)
    implementation(projects.feature.pokemonDetail)
    implementation(projects.feature.pokemonList)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.koin.android)
    implementation(libs.kotlinx.serialization.core)

    testImplementation(libs.androidx.paging.common)
}
