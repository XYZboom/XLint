import java.time.ZonedDateTime
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

dependencies {
    api("org.jgrapht:jgrapht-core:1.5.2")
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.4.0")
    implementation("org.slf4j:slf4j-api:2.0.12")
    implementation("org.yaml:snakeyaml:2.2")
    api("ch.qos.logback:logback-classic:1.4.14")
    implementation("org.mvel:mvel2:2.5.0.Final")
    implementation("org.reflections:reflections:0.10.2")
    compileOnly("org.jetbrains.kotlin:kotlin-compiler:2.4.0")

    implementation(project(":xlint-kotlin-analysis-api"))
    implementation(project(":xlint-kotlin-analysis-api-standalone"))
}

tasks.register("writeProperties") {
    doLast {
        val properties = mapOf(
            "meta.XLintVersion" to project.version,
            "meta.XLintBuildTime" to ZonedDateTime.now()
        )

        val content = properties.entries.joinToString("\n") { (key, value) ->
            "$key=$value"
        }

        val resourceDir = sourceSets.main.get().resources.srcDirs.first()
        val propertyFile = File(resourceDir, "xlint-meta.properties")
        if (!propertyFile.exists()) {
            propertyFile.createNewFile()
        }

        propertyFile.writeText(content)
    }
}

tasks.getByName("compileJava") {
    dependsOn("writeProperties")
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.compilerOptions {
    freeCompilerArgs.set(listOf("-Xcontext-parameters"))
}