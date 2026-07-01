package io.github.xyzboom.xlint

import com.github.tnoalex.foundation.ApplicationContext
import com.github.tnoalex.foundation.bean.container.SimpleSingletonBeanContainer
import com.github.tnoalex.specs.AnalyzerSpec
import io.github.xyzboom.xlint.compiler.ICompilerEnvContext
import io.github.xyzboom.xlint.config.loadConfig
import io.github.xyzboom.xlint.processor.IJavaProcessor
import io.github.xyzboom.xlint.processor.IKotlinProcessor
import io.github.xyzboom.xlint.processor.IProcessorProvider
import java.util.ServiceLoader

class XLintApplication private constructor(
    private val analyzerSpec: AnalyzerSpec,
    private val compilerEnvContext: ICompilerEnvContext
) {
    companion object {
        @JvmStatic
        operator fun invoke(analyzerSpec: AnalyzerSpec, compiler: ICompilerEnvContext): XLintApplication {
            return XLintApplication(analyzerSpec, compiler)
        }
    }

    fun run() {
        compilerEnvContext.runAnalyze {
            val context = XLintContext(this, analyzerSpec.confidenceLevel)
            val providers = ServiceLoader.load(IProcessorProvider::class.java)
            // todo: remove this line after migrating to XLint
            ApplicationContext.removeBean("Context")
            ApplicationContext.addBean("Context", context, SimpleSingletonBeanContainer)
            val processors = if (analyzerSpec.debugSpec.disableAnyElse.isNotEmpty()) {
                providers.flatMap { it.getProcessorFromNames(analyzerSpec.debugSpec.disableAnyElse) }
            } else {
                providers.flatMap { it.getAllProcessors() }
            }

            // Load config once and inject into each processor
            val config = loadConfig(analyzerSpec.extendRulePath)
            for (processor in processors) {
                processor.configure(config)
            }

            with(context) {
                for (processor in processors) {
                    processor.onBeforeProcess()
                }
                for (ktSource in compilerEnvContext.ktSourceFiles) {
                    for (processor in processors.asSequence().filterIsInstance<IKotlinProcessor>()) {
                        processor.process(ktSource)
                    }
                }
                for (javaSource in compilerEnvContext.javaSourceFiles) {
                    for (processor in processors.asSequence().filterIsInstance<IJavaProcessor>()) {
                        processor.process(javaSource)
                    }
                }
                for (processor in processors) {
                    processor.onAfterProcess()
                }
            }
        }
    }
}