package io.github.xyzboom.xlint.processor

import org.jetbrains.kotlin.psi.KtFile

interface IKotlinProcessor : IProcessor {
    fun process(file: KtFile)
}