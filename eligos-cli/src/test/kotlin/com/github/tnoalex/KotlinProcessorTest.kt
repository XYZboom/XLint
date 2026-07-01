package com.github.tnoalex

import com.github.tnoalex.issues.kotlin.*
import com.github.tnoalex.processor.kotlin.*
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class KotlinProcessorTest {

    @Test
    fun testImplicitSingleExprFunction() {
        runWithCompilerEnv("resources@implicitSingleExprFunction") { env ->
            val processor = ImplicitSingleExprFunctionProcessor()
            runProcessorOnAllKtFiles(env, this, processor)
            val issues = collectIssues<ImplicitSingleExprFunctionIssue>(this)
            assertEquals(1, issues.size)
            assertArrayEquals(
                arrayOf<Any?>(6, "fun test0() = java.lang.String.valueOf(1)"),
                issues.firstOrNull()?.let {
                    arrayOf<Any?>(it.startLine, it.content)
                }
            )
        }
    }

    @Test
    fun testKotlinMccabeComplexity() {
        runWithCompilerEnv("resources@complexMethods") { env ->
            val processor = KotlinMccabeComplexityProcessor()
            runProcessorOnAllKtFiles(env, this, processor)
            val issues = collectIssues<ComplexKotlinFunctionIssue>(this)
            assertEquals(1, issues.size)
            assertArrayEquals(
                arrayOf(10, 17),
                issues.firstOrNull()?.let {
                    arrayOf(it.circleComplexity, it.startLine)
                }
            )
        }
    }

    @Test
    fun testObjectExtendsThrowable() {
        runWithCompilerEnv("resources@objectExtendsThrowable") { env ->
            val processor = ObjectExtendsThrowableProcessor()
            runProcessorOnAllKtFiles(env, this, processor)
            val issues = collectIssues<ObjectExtendsThrowableIssue>(this)
            assertEquals(1, issues.size)
            assertEquals(
                "objectExtendsThrowable.ExtendsThrowable",
                issues.firstOrNull()?.objectFqName
            )
        }
    }

    @Test
    fun testOptimizedTailRecursion() {
        runWithCompilerEnv("resources@optimizedTailRecursion") { env ->
            val processor = TailRecursionProcessor()
            runProcessorOnAllKtFiles(env, this, processor)
            val issues = collectIssues<OptimizedTailRecursionIssue>(this)
            assertEquals(2, issues.size)
            assertEquals(
                "optimizedTailRecursion.factorial0(n,acc)",
                issues.firstOrNull { it.startLine == 3 }
                    ?.functionSignature
            )
            assertEquals(
                "optimizedTailRecursion.factorial4(n,acc)",
                issues.firstOrNull { it.startLine == 32 }
                    ?.functionSignature
            )
        }
    }

    @Test
    fun testWhenInsteadOfCascadeIf() {
        runWithCompilerEnv("resources@whenInsteadOfCascadeIf") { env ->
            val processor = WhenInsteadOfCascadeIfProcessor()
            runProcessorOnAllKtFiles(env, this, processor)
            val issues = collectIssues<WhenInsteadOfCascadeIfIssue>(this)
            assertEquals(1, issues.size)
            assertArrayEquals(
                arrayOf(4, 4),
                issues.firstOrNull()?.let {
                    arrayOf(it.startLine, it.cascadeDepth)
                }
            )
        }
    }

    @Test
    fun testCompareDataObjectWithReference() {
        runWithCompilerEnv("resources@compareDataObjectWithReference") { env ->
            val processor = CompareDataObjectWithReferenceProcessor()
            runProcessorOnAllKtFiles(env, this, processor)
            val issues = collectIssues<CompareDataObjectWithReferenceIssue>(this)
            assertEquals(1, issues.size)
            assertArrayEquals(
                arrayOf<Any?>("rdobject", "dobject", 8),
                issues.firstOrNull()?.let {
                    arrayOf<Any?>(it.leftPropertyFqName, it.rightPropertyFqName, it.startLine)
                }
            )
        }
    }
}