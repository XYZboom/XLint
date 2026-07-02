import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.google.devtools.ksp")
}

dependencies {
    api(project(":xlint-core"))
    implementation(project(":xlint-ksp-annotations"))
    ksp(project(":xlint-ksp"))
    compileOnly("org.jetbrains.kotlin:kotlin-compiler:2.4.0")
    implementation(project(":xlint-issues"))
    implementation(project(":xlint-kotlin-analysis-api"))
    implementation(project(":xlint-kotlin-analysis-api-standalone"))
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.compilerOptions {
    freeCompilerArgs.set(listOf("-Xcontext-parameters"))
}