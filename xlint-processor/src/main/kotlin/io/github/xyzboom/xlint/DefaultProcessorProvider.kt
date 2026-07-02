package io.github.xyzboom.xlint

import io.github.xyzboom.xlint.annotations.ProcessorProvider
import io.github.xyzboom.xlint.processor.IProcessor
import io.github.xyzboom.xlint.processor.IProcessorProvider

@ProcessorProvider
class DefaultProcessorProvider : IProcessorProvider {
    override fun getProcessorFromNames(names: Collection<String>): Collection<IProcessor> {
        return names.mapNotNull { name ->
            xLintProcessorMap[name]?.invoke()
        }
    }

    override fun getAllProcessors(): Collection<IProcessor> {
        return xLintProcessorMap.values.map { it() }
    }
}