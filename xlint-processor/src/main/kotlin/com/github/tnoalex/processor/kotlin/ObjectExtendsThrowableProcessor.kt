package com.github.tnoalex.processor.kotlin

import com.github.tnoalex.issues.Severity
import com.github.tnoalex.issues.kotlin.ObjectExtendsThrowableIssue
import com.github.tnoalex.processor.utils.filePath
import com.github.tnoalex.processor.utils.nameCanNotResolveWarn
import com.github.tnoalex.processor.utils.superTypes
import com.intellij.lang.Language
import io.github.xyzboom.xlint.XLintContext
import io.github.xyzboom.xlint.annotations.Processor
import io.github.xyzboom.xlint.processor.IKotlinProcessor
import io.github.xyzboom.xlint.visitor.KtXLintTreeVisitorVoid
import io.github.xyzboom.xlint.visitor.analyze
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.utils.addToStdlib.ifTrue
import org.slf4j.LoggerFactory

@Processor
class ObjectExtendsThrowableProcessor : IKotlinProcessor {
    override val severity: Severity
        get() = Severity.CODE_SMELL
    override val supportLanguage: List<Language>
        get() = listOf(KotlinLanguage.INSTANCE)

    context(context: XLintContext)
    override fun process(file: KtFile) {
        file.accept(ObjectVisitor(context))
    }

    private class ObjectVisitor(context: XLintContext) : KtXLintTreeVisitorVoid(context) {

        override fun visitObjectDeclaration(declaration: KtObjectDeclaration) {
            if (declaration.isCompanion()) return super.visitObjectDeclaration(declaration)

            analyze {
                declaration.superTypes.any { type ->
                    type.isSubtypeOf(StandardClassIds.Throwable)
                }
            }.ifTrue {
                context.reportIssue(
                    ObjectExtendsThrowableIssue(
                    declaration.filePath,
                    declaration.fqName?.asString() ?: let {
                        logger.nameCanNotResolveWarn("object", declaration)
                        "unknown object name"
                    }
                )
                )
            }
            super.visitObjectDeclaration(declaration)
        }
    }

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(ObjectExtendsThrowableProcessor::class.java)
    }
}