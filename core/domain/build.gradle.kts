plugins {
    id("pokedex.kotlin.jvm")
    id("pokedex.testing")
}

dependencies {
    api(projects.core.model)
    implementation(libs.androidx.paging.common)
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.androidx.paging.testing)
}
