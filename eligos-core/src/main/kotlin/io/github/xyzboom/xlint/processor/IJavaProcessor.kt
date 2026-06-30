package io.github.xyzboom.xlint.processor

import com.intellij.psi.PsiJavaFile

interface IJavaProcessor : IProcessor {
    fun process(file: PsiJavaFile)
}