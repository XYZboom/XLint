package com.github.tnoalex.processor.stats

import com.github.tnoalex.processor.utils.lineCount
import com.github.tnoalex.statistics.JavaStatistics
import com.intellij.lang.Language
import com.intellij.lang.java.JavaLanguage
import com.intellij.psi.JavaRecursiveElementVisitor
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiMethod
import io.github.xyzboom.xlint.XLintContext
import io.github.xyzboom.xlint.annotations.Processor
import io.github.xyzboom.xlint.processor.IJavaProcessor

@Processor
class JavaStatisticsProcessor : IJavaProcessor {
    private var stats = JavaStatistics()
    override val supportLanguage: List<Language>
        get() = listOf(JavaLanguage.INSTANCE)

    context(context: XLintContext)
    override fun process(file: PsiJavaFile) {
        stats.fileNumber++
        stats.lineNumber += file.lineCount
        file.accept(javaVisitor)
    }

    context(context: XLintContext)
    override fun onAfterProcess() {
        context.reportStatistics(stats)
        stats = JavaStatistics()
    }

    private val javaVisitor = object : JavaRecursiveElementVisitor() {
        override fun visitClass(aClass: PsiClass) {
            stats.classNumber++
            super.visitClass(aClass)
        }

        override fun visitMethod(method: PsiMethod) {
            stats.methodNumber++
            super.visitMethod(method)
        }
    }
}