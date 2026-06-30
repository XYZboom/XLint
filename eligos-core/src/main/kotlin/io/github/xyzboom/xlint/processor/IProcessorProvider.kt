package io.github.xyzboom.xlint.processor

interface IProcessorProvider {
    fun getProcessorFromNames(names: Collection<String>): Collection<IProcessor>

    fun getAllProcessors(): Collection<IProcessor>
}