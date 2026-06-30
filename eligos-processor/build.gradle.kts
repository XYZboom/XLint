import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.google.devtools.ksp")
}

dependencies {
    api(project(":eligos-core"))
    implementation(project(":xlint-ksp-annotations"))
    ksp(project(":xlint-ksp"))
    compileOnly("org.jetbrains.kotlin:kotlin-compiler:2.4.0")
    implementation(project(":eligos-issues"))
    implementation(project(":eligos-kotlin-analysis-api"))
    implementation(project(":eligos-kotlin-analysis-api-standalone"))
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.compilerOptions {
    freeCompilerArgs.set(listOf("-Xcontext-parameters"))
}