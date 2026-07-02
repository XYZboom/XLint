package io.github.xyzboom.xlint.processor

import io.github.xyzboom.xlint.XLintContext
import org.jetbrains.kotlin.psi.KtFile

interface IKotlinProcessor : IProcessor {
    context(context: XLintContext)
    fun process(file: KtFile)
}