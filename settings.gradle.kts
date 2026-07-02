pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

rootProject.name = "XLint"
include("xlint-cli")
include("xlint-core")
include("xlint-processor")
/*include(
    ":kt-references-analysis:analysis-api",
    ":kt-references-analysis:analysis-api-fe10",
    ":kt-references-analysis:analysis-api-impl-base",
    ":kt-references-analysis:analysis-internal-utils",
    ":kt-references-analysis:kt-references-fe10")*/
include("xlint-issues")
include("xlint-formatter")
include("xlint-kotlin-analysis-api")
include("xlint-kotlin-analysis-api-standalone")
include("xlint-ksp")
include("xlint-ksp-annotations")