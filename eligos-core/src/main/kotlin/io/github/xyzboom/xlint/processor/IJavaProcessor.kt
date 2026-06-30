package io.github.xyzboom.xlint.processor

import com.intellij.psi.PsiJavaFile
import io.github.xyzboom.xlint.XLintContext

interface IJavaProcessor : IProcessor {
    context(_: XLintContext)
    fun process(file: PsiJavaFile)
}