package com.github.tnoalex.processor.kotlin.withJava

import com.github.tnoalex.issues.Severity
import com.github.tnoalex.issues.kotlin.withJava.optional.ParameterOptionalIssue
import com.github.tnoalex.issues.kotlin.withJava.optional.PropertyIsOptionalIssue
import com.github.tnoalex.issues.kotlin.withJava.optional.ReturnOptionalIssue
import com.github.tnoalex.processor.utils.checkAnyRecursively
import com.github.tnoalex.processor.utils.filePath
import com.github.tnoalex.processor.utils.startLine
import com.intellij.lang.Language
import com.intellij.lang.java.JavaLanguage
import io.github.xyzboom.xlint.XLintContext
import io.github.xyzboom.xlint.annotations.Processor
import io.github.xyzboom.xlint.processor.IKotlinProcessor
import io.github.xyzboom.xlint.visitor.KtXLintTreeVisitorVoid
import io.github.xyzboom.xlint.visitor.analyze
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.types.KaType
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.containingClass
import org.slf4j.LoggerFactory

@Processor
class OptionalInKotlinProcessor : IKotlinProcessor {
    override val severity: Severity = Severity.CODE_SMELL
    override val supportLanguage: List<Language>
        get() = listOf(JavaLanguage.INSTANCE, KotlinLanguage.INSTANCE)

    context(context: XLintContext)
    override fun process(file: KtFile) {
        file.accept(Visitor(context))
    }

    private class Visitor(context: XLintContext) : KtXLintTreeVisitorVoid(context) {
        override fun visitProperty(property: KtProperty) {
            analyze {
                checkProperty(property)
            }
            super.visitProperty(property)
        }

        override fun visitNamedFunction(function: KtNamedFunction) {
            checkFunction(function)
            super.visitNamedFunction(function)
        }

        private fun checkFunction(function: KtNamedFunction) {
            analyze {
                checkReturnType(function)
                checkParameters(function)
            }
        }

        private fun KaSession.checkReturnType(function: KtNamedFunction) {
            val returnType = function.symbol.returnType
            if (checkAnyRecursively(returnType) { isOptional(it) }) {
                context.reportIssue(
                    ReturnOptionalIssue(
                        function.filePath,
                        function.containingClass()?.fqName?.asString() ?: "anonymous kotlin class",
                        function.name ?: "anonymous kotlin function",
                        function.startLine
                    )
                )
            }
        }

        private fun KaSession.checkParameters(function: KtNamedFunction) {
            val valueParameters = function.valueParameters
            val optionalIndices = mutableListOf<Int>()
            for ((index, parameter) in valueParameters.withIndex()) {
                val parameterType = parameter.expressionType ?: continue
                if (checkAnyRecursively(parameterType) { isOptional(it) }) {
                    optionalIndices.add(index)
                }
            }
            if (optionalIndices.isNotEmpty()) {
                context.reportIssue(
                    ParameterOptionalIssue(
                        function.filePath,
                        function.containingClass()?.fqName?.asString() ?: "anonymous kotlin class",
                        function.name ?: "anonymous kotlin function",
                        function.startLine,
                        optionalIndices
                    )
                )
            }
        }

        private fun KaSession.isOptional(kotlinType: KaType): Boolean {
            return kotlinType.isClassType(OPTIONAL_CLASS_ID)
        }

        private fun KaSession.checkProperty(property: KtProperty) {
            val propertyType = property.expressionType ?: return
            if (checkAnyRecursively(propertyType) { isOptional(it) }) {
                context.reportIssue(
                    PropertyIsOptionalIssue(
                        property.filePath,
                        property.name ?: "anonymous property",
                        property.startLine,
                        property.isTopLevel,
                        property.isLocal
                    )
                )
            }
        }
    }

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(UncertainNullablePlatformTypeProcessor::class.java)

        private val OPTIONAL_CLASS_ID = ClassId.fromString("java/util/Optional")
    }
}