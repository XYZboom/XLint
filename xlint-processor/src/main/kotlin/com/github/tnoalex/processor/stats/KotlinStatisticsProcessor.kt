package com.github.tnoalex.processor.stats

import com.github.tnoalex.processor.utils.lineCount
import com.github.tnoalex.statistics.KotlinStatistics
import com.intellij.lang.Language
import io.github.xyzboom.xlint.XLintContext
import io.github.xyzboom.xlint.annotations.Processor
import io.github.xyzboom.xlint.processor.IKotlinProcessor
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.psi.*


@Processor
class KotlinStatisticsProcessor : IKotlinProcessor {
    private var stats = KotlinStatistics()
    override val supportLanguage: List<Language>
        get() = listOf(KotlinLanguage.INSTANCE)

    context(context: XLintContext)
    override fun process(file: KtFile) {
        stats.fileNumber++
        stats.lineNumber += file.lineCount
        file.accept(ktVisitor)
    }

    context(context: XLintContext)
    override fun onAfterProcess() {
        context.reportStatistics(stats)
        stats = KotlinStatistics()
    }

    private val ktVisitor = object : KtTreeVisitorVoid() {
        override fun visitClass(klass: KtClass) {
            stats.classNumber++
            super.visitClass(klass)
        }

        override fun visitProperty(property: KtProperty) {
            stats.propertyNumber++
            super.visitProperty(property)
        }

        override fun visitNamedFunction(function: KtNamedFunction) {
            stats.functionNumber++
            super.visitNamedFunction(function)
        }
    }
}