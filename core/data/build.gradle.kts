plugins {
    id("pokedex.android.library")
    id("pokedex.testing")
}

android {
    namespace = "com.jnasser.pokedex.core.data"
}

dependencies {
    implementation(projects.core.database)
    implementation(projects.core.domain)
    implementation(projects.core.model)
    implementation(projects.core.network)

    implementation(libs.androidx.paging.common)
    implementation(libs.androidx.room.ktx)
    implementation(libs.koin.core)
    implementation(libs.kotlinx.coroutines.core)
}
