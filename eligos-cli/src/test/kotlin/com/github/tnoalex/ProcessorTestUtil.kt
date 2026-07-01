package com.github.tnoalex

import com.github.tnoalex.issues.Issue
import com.github.tnoalex.parser.CliCompilerEnvironmentContext
import com.github.tnoalex.specs.KotlinCompilerSpec
import com.intellij.psi.PsiJavaFile
import io.github.xyzboom.xlint.XLintContext
import org.jetbrains.annotations.TestOnly
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtFile
import java.io.File
import java.nio.file.Paths

@TestOnly
fun createCompilerContext(sourcePath: String): CliCompilerEnvironmentContext {
    val path = resolveTestResources(sourcePath)
    val compilerSpec = KotlinCompilerSpec(
        Paths.get(path),
        listOf(Paths.get(path)),
        defaultJdkHome,
        kotlinStdLibPath = defaultKotlinLib,
        disableCompilerLog = true
    )
    return CliCompilerEnvironmentContext(compilerSpec)
}

@TestOnly
inline fun <reified T : Issue> collectIssues(context: XLintContext): List<T> {
    return context.issues.filterIsInstance<T>()
}

@TestOnly
fun runWithCompilerEnv(
    sourcePath: String,
    block: XLintContext.(env: CliCompilerEnvironmentContext) -> Unit
) {
    val compilerEnv = createCompilerContext(sourcePath)
    compilerEnv.use { compilerEnv ->
        analyze(compilerEnv.module) {
            val context = XLintContext(this)
            with(context) {
                block(compilerEnv)
            }
        }
    }
}

/**
 * Run a processor against all Kotlin source files in the compiler context.
 */
@TestOnly
fun runProcessorOnAllKtFiles(
    env: CliCompilerEnvironmentContext,
    context: XLintContext,
    processor: io.github.xyzboom.xlint.processor.IKotlinProcessor
) {
    for (ktFile in env.allSourceFiles.filterIsInstance<KtFile>()) {
        with(context) {
            processor.process(ktFile)
        }
    }
}

/**
 * Run a processor against all Java source files.
 */
@TestOnly
fun runProcessorOnAllJavaFiles(
    env: CliCompilerEnvironmentContext,
    context: XLintContext,
    processor: io.github.xyzboom.xlint.processor.IJavaProcessor
) {
    for (javaFile in env.allSourceFiles.filterIsInstance<PsiJavaFile>()) {
        with(context) {
            processor.process(javaFile)
        }
    }
}

@TestOnly
fun resolveTestResources(resourcePath: String): String {
    if (resourcePath.startsWith("resources@")) {
        val resourcesPath = resourcePath.removePrefix("resources@")
        var path = Thread.currentThread().contextClassLoader.getResource(resourcesPath)?.path
        val isWindows = System.getProperty("os.name").lowercase().contains("windows")
        if (isWindows) {
            path = path?.removePrefix("/")
        }
        return path ?: resourcesPath
    } else if (resourcePath.startsWith("file@")) {
        return resourcePath.removePrefix("file@")
    } else {
        throw RuntimeException("Cannot load test resources from $resourcePath")
    }
}

private val defaultJdkHome = File(System.getProperty("java.home")).toPath()
private val defaultKotlinLib = File(CharRange::class.java.protectionDomain.codeSource.location.path).toPath()