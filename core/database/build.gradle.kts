plugins {
    id("pokedex.android.library")
    id("pokedex.room")
    id("pokedex.testing")
}

android {
    namespace = "com.jnasser.pokedex.core.database"
}

dependencies {
    implementation(libs.koin.android)
}
