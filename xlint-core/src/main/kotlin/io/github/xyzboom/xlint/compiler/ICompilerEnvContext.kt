package io.github.xyzboom.xlint.compiler

import com.intellij.psi.PsiJavaFile
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.psi.KtFile

interface ICompilerEnvContext {
    fun runAnalyze(action: KaSession.() -> Unit)
    val ktSourceFiles: Collection<KtFile>
    val javaSourceFiles: Collection<PsiJavaFile>
}