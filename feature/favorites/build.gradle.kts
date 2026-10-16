plugins {
    id("pokedex.android.library")
    id("pokedex.android.compose")
    id("pokedex.testing")
}

android {
    namespace = "com.jnasser.pokedex.feature.favorites"
}

dependencies {
    implementation(projects.core.designsystem)
    implementation(projects.core.domain)
    implementation(projects.core.model)

    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)

    testImplementation(libs.androidx.paging.common)
}
