plugins {
    id("pokedex.android.library")
    id("pokedex.android.compose")
    id("pokedex.testing")
}

android {
    namespace = "com.jnasser.pokedex.core.designsystem"
}

dependencies {
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.material.icons.core)
}
