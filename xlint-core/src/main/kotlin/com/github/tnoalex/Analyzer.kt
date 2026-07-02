package com.github.tnoalex

import com.github.tnoalex.foundation.ApplicationContext
import com.github.tnoalex.specs.AnalyzerSpec
import io.github.xyzboom.xlint.IContext
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

class Analyzer(private val analyzerSpec: AnalyzerSpec) {
    val context: IContext = ApplicationContext.getExactBean(IContext::class.java)!!.also {
        it.confidenceLevel = analyzerSpec.confidenceLevel
    }
    private var analyzerInitialized = false

    fun analyze() {
        logger.info("Start analyzing")
        if (!analyzerInitialized) {
            ApplicationContext.launchEnvironment = analyzerSpec.launchEnvironment
            ApplicationContext.solveComponentEnv()
            setTopExceptionHandle()
        }
        context.resetContext()
        analyzerInitialized = true
        logger.info("Analyzing done")
    }

    private fun enableAllLangs(): Boolean {
        return listOfNotNull(analyzerSpec.majorLang, analyzerSpec.withLang).contains("any")
    }

    private fun setTopExceptionHandle() {
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            if (analyzerSpec.debugSpec.enabledDebug) {
                e.printStackTrace()
            } else {
                if (analyzerSpec.exceptionHandler == null) {
                    logger.error("Something went wrong..... Turn on debug to see the details or contact us on github")
                    exitProcess(-1)
                } else {
                    analyzerSpec.exceptionHandler.invoke(t, e)
                }
            }
        }
    }

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(Analyzer::class.java)
    }
}