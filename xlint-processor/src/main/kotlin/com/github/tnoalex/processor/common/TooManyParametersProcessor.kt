package com.github.tnoalex.processor.common

import com.github.tnoalex.issues.Severity
import com.github.tnoalex.issues.common.ExcessiveParamsIssue
import com.github.tnoalex.processor.utils.filePath
import com.github.tnoalex.processor.utils.nameCanNotResolveWarn
import com.github.tnoalex.processor.utils.startLine
import com.intellij.lang.Language
import com.intellij.lang.java.JavaLanguage
import com.intellij.psi.JavaRecursiveElementVisitor
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiParameter
import io.github.xyzboom.xlint.XLintContext
import io.github.xyzboom.xlint.annotations.Processor
import io.github.xyzboom.xlint.config.ConfigProvider
import io.github.xyzboom.xlint.processor.IJavaProcessor
import io.github.xyzboom.xlint.processor.IKotlinProcessor
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid
import org.slf4j.LoggerFactory

@Processor
class TooManyParametersProcessor : IJavaProcessor, IKotlinProcessor {
    override val severity: Severity = Severity.CODE_SMELL
    override val supportLanguage: List<Language>
        get() = listOf(JavaLanguage.INSTANCE, KotlinLanguage.INSTANCE)

    private var arity: Int = 6

    override fun configure(config: ConfigProvider) {
        this.arity = config.getInt("function.arity", 6)
    }

    context(context: XLintContext)
    override fun process(file: PsiJavaFile) {
        file.accept(object : JavaRecursiveElementVisitor() {
            override fun visitMethod(method: PsiMethod) {
                if (method.parameterList.parameters.size >= arity) {
                    with(method) {
                        context.reportIssue(
                            ExcessiveParamsIssue(
                                containingFile.virtualFile.path, name,
                                parameterList.parameters.map { (it as PsiParameter).text },
                                startLine,
                                parameterList.parameters.size
                            )
                        )
                    }
                }
                super.visitMethod(method)
            }
        })
    }

    context(context: XLintContext)
    override fun process(file: KtFile) {
        file.accept(object : KtTreeVisitorVoid() {
            override fun visitNamedFunction(function: KtNamedFunction) {
                if (function.valueParameters.size > arity) {
                    with(function) {
                        context.reportIssue(
                            ExcessiveParamsIssue(
                                filePath,
                                fqName?.asString() ?: let {
                                    logger.nameCanNotResolveWarn("function", this)
                                    "unknown func"
                                },
                                function.valueParameters.map { p ->
                                    p.name ?: let {
                                        logger.nameCanNotResolveWarn("parameter", p)
                                        "unknown param"
                                    }
                                },
                                function.startLine,
                                valueParameters.size
                            )
                        )
                    }
                }
                super.visitNamedFunction(function)
            }
        })
    }

    companion object {
        private val logger = LoggerFactory.getLogger(TooManyParametersProcessor::class.java)
    }
}
