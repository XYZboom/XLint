package com.github.tnoalex.processor.common

import com.github.tnoalex.issues.Severity
import com.github.tnoalex.issues.common.CircularReferencesIssue
import com.github.tnoalex.processor.utils.refCanNotResolveWarn
import com.github.tnoalex.processor.utils.referenceExpressionSelfOrInChildren
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import io.github.xyzboom.xlint.XLintContext
import io.github.xyzboom.xlint.annotations.Processor
import io.github.xyzboom.xlint.processor.IJavaProcessor
import io.github.xyzboom.xlint.processor.IKotlinProcessor
import org.jetbrains.kotlin.analysis.decompiler.psi.file.KtDecompiledFile
import org.jetbrains.kotlin.asJava.elements.KtLightElement
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtPackageDirective
import org.jetbrains.kotlin.psi.KtReferenceExpression
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid
import org.jetbrains.kotlin.psi.psiUtil.isInImportDirective
import org.jgrapht.Graph
import org.jgrapht.alg.connectivity.GabowStrongConnectivityInspector
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.builder.GraphTypeBuilder
import org.slf4j.LoggerFactory

@Processor
class CircularReferencesProcessor : IJavaProcessor, IKotlinProcessor {
    companion object {
        private val logger = LoggerFactory.getLogger(CircularReferencesProcessor::class.java)
    }

    override val severity: Severity = Severity.CODE_SMELL

    private var dependencyGraph = newEmptyGraph()

    context(context: XLintContext)
    override fun onAfterProcess() {
        val sccAlg = GabowStrongConnectivityInspector(dependencyGraph)
        val scc = sccAlg.stronglyConnectedComponents.filter { it.vertexSet().size > 1 } // Outliers also is scc
        scc.forEach {
            val subGraph = newEmptyGraph()
            it.vertexSet().forEach { v -> subGraph.addVertex(v) }
            it.edgeSet().forEach { e -> subGraph.addEdge(it.getEdgeSource(e), it.getEdgeTarget(e)) }
            context.reportIssue(CircularReferencesIssue(it.vertexSet().toHashSet(), subGraph))
        }
        dependencyGraph = newEmptyGraph()
    }

    private fun newEmptyGraph(): Graph<String, DefaultEdge> {
        return GraphTypeBuilder.directed<String, DefaultEdge>().allowingMultipleEdges(false)
            .allowingSelfLoops(false).edgeClass(DefaultEdge::class.java).weighted(false).buildGraph()
    }

    fun resolveRef(providerElement: PsiElement, consumeFile: String) {
        if (PsiTreeUtil.getParentOfType(providerElement, KtDecompiledFile::class.java) != null) return
        if (PsiTreeUtil.getParentOfType(providerElement, PsiCompiledElement::class.java) != null) return
        val srcElement =
            if (providerElement is KtLightElement<*, *>) providerElement.kotlinOrigin!! else providerElement
        PsiTreeUtil.getParentOfType(srcElement, PsiJavaFile::class.java)?.let {
            it.virtualFile ?: return
            addDependency(it.virtualFile.path, consumeFile)
            return
        }
        PsiTreeUtil.getParentOfType(srcElement, KtFile::class.java)?.let {
            it.virtualFile ?: return
            addDependency(it.virtualFilePath, consumeFile)
            return
        }
    }

    private fun addDependency(providerFile: String, consumeFile: String) {
        if (providerFile == consumeFile) return
        dependencyGraph.addVertex(providerFile)
        dependencyGraph.addEdge(providerFile, consumeFile)
    }

    context(context: XLintContext)
    override fun process(file: PsiJavaFile) {
        val fileName = file.virtualFile.path
        dependencyGraph.addVertex(fileName)
        file.accept(object : JavaRecursiveElementVisitor() {
            override fun visitReferenceElement(reference: PsiJavaCodeReferenceElement) {
                if (PsiTreeUtil.getParentOfType(reference, PsiPackageStatement::class.java) != null)
                    return super.visitReferenceElement(reference)
                if (PsiTreeUtil.getParentOfType(reference, PsiImportStatement::class.java) != null)
                    return super.visitReferenceElement(reference)
                try {
                    reference.resolve()?.let {
                        resolveRef(it, fileName)
                    }
                } catch (_: RuntimeException) {
                    logger.refCanNotResolveWarn(reference)
                }
                super.visitReferenceElement(reference)
            }
        })
    }

    context(context: XLintContext)
    override fun process(file: KtFile) {
        val fileName = file.virtualFilePath
        dependencyGraph.addVertex(fileName)
        file.accept(object : KtTreeVisitorVoid() {
            override fun visitReferenceExpression(expression: KtReferenceExpression) {
                if (expression.isInImportDirective()) return super.visitReferenceExpression(expression)
                if (PsiTreeUtil.getParentOfType(expression, KtPackageDirective::class.java) != null)
                    return super.visitReferenceExpression(expression)
                expression.referenceExpressionSelfOrInChildren().forEach {
                    try {
                        it.references.forEach { ref ->
                            ref.resolve()?.let { r -> resolveRef(r, fileName) }
                        }
                    } catch (_: RuntimeException) {
                        logger.refCanNotResolveWarn(expression)
                    }
                }
                super.visitReferenceExpression(expression)
            }
        })
    }
}
