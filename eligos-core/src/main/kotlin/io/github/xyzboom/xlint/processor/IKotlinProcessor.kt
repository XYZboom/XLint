package io.github.xyzboom.xlint.processor

import io.github.xyzboom.xlint.XLintContext
import org.jetbrains.kotlin.psi.KtFile

interface IKotlinProcessor : IProcessor {
    context(_: XLintContext)
    fun process(file: KtFile)
}