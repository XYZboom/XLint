package com.github.tnoalex.processor.kotlin

import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.bean.Suitable
import com.github.tnoalex.foundation.eventbus.EventListener
import com.github.tnoalex.foundation.language.KotlinLanguage
import com.github.tnoalex.foundation.language.Language
import com.github.tnoalex.issues.Severity
import com.github.tnoalex.issues.kotlin.ObjectExtendsThrowableIssue
import com.github.tnoalex.processor.IssueProcessor
import com.github.tnoalex.processor.utils.filePath
import com.github.tnoalex.processor.utils.nameCanNotResolveWarn
import com.github.tnoalex.processor.utils.superTypes
import com.intellij.psi.PsiFile
import io.github.xyzboom.xlint.XLintContext
import io.github.xyzboom.xlint.annotations.Processor
import io.github.xyzboom.xlint.processor.IKotlinProcessor
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid
import org.jetbrains.kotlin.utils.addToStdlib.ifTrue
import org.slf4j.LoggerFactory

@Component
@Suitable(LaunchEnvironment.CLI)
@Processor
class ObjectExtendsThrowableProcessor : IssueProcessor, IKotlinProcessor {
    override val severity: Severity
        get() = Severity.CODE_SMELL
    override val supportLanguage: List<Language>
        get() = listOf(KotlinLanguage)

    @EventListener(filterClazz = [KtFile::class])
    override fun process(psiFile: PsiFile) {
        psiFile.accept(objectVisitor)
    }

    context(_: XLintContext)
    override fun process(file: KtFile) {
        file.accept(objectVisitor)
    }

    private val objectVisitor = object : KtTreeVisitorVoid() {

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