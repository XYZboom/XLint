plugins {
    id("com.google.devtools.ksp") version "2.3.9" apply false
}

dependencies {
    implementation(project(":eligos-core"))
    implementation(project(":xlint-ksp-annotations"))
    implementation("com.google.devtools.ksp:symbol-processing-api:2.3.6")
    implementation("com.squareup:kotlinpoet:2.3.0")
    implementation("com.squareup:kotlinpoet-ksp:2.3.0")
}