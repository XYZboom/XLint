package com.github.tnoalex

import com.github.tnoalex.issues.ConfidenceLevel
import com.github.tnoalex.issues.kotlin.withJava.*
import com.github.tnoalex.issues.kotlin.withJava.internalExpose.JavaExtendOrImplInternalKotlinIssue
import com.github.tnoalex.issues.kotlin.withJava.internalExpose.JavaParameterInternalKotlinIssue
import com.github.tnoalex.issues.kotlin.withJava.internalExpose.JavaReturnInternalKotlinIssue
import com.github.tnoalex.issues.kotlin.withJava.nonnullAssertion.NonNullAssertionOnNullableTypeIssue
import com.github.tnoalex.issues.kotlin.withJava.nonnullAssertion.NonNullAssertionOnPlatformTypeIssue
import com.github.tnoalex.processor.kotlin.withJava.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class KotlinWithJavaProcessorTest {

    @Test
    fun testIgnoredException() {
        runWithCompilerEnv("resources@ignoreException") { env ->
            val processor = IgnoredExceptionProcessor()
            runProcessorOnAllKtFiles(env, this, processor)
            runProcessorOnAllJavaFiles(env, this, processor)
            val issues = collectIssues<IgnoredExceptionIssue>(this)
            assertEquals(1, issues.size)
            assertArrayEquals(
                arrayOf<Any?>(8, "java.io.IOException", true),
                issues.firstOrNull()?.let {
                    arrayOf<Any?>(it.startLine, it.ignoredExceptions, it.calledByJava)
                }
            )
        }
    }

    @Test
    fun testIncomprehensibleJavaFacadeName() {
        runWithCompilerEnv("resources@incomprehensibleJavaFacadeName") { env ->
            val processor = IncomprehensibleJavaFacadeNameProcessor()
            runProcessorOnAllKtFiles(env, this, processor)
            val issues = collectIssues<IncomprehensibleJavaFacadeNameIssue>(this)
            assertEquals(1, issues.size)
            assertArrayEquals(
                arrayOf<Any?>(true, true, "IncomprehensibleClassNameKt"),
                issues.firstOrNull()?.let {
                    arrayOf<Any?>(it.hasTopLevelFunction, it.hasTopLevelProperty, it.javaFacadeName)
                }
            )
        }
    }

    @Test
    fun testInternalExposedGeneric() {
        runWithCompilerEnv("resources@internalExposed/generic") { env ->
            val processor = InternalExposedProcessor()
            runProcessorOnAllJavaFiles(env, this, processor)
            val issues = collectIssues<JavaExtendOrImplInternalKotlinIssue>(this)
            assertEquals(1, issues.size)
            val issue = issues.single()
            assertEquals(
                hashSetOf("A.java", "KtInternal.kt"),
                issue.affectedFiles.map { it.split("/").last() }.toHashSet()
            )
            assertEquals(hashSetOf("KtInternal", "IKtInternal0"), issue.exposedTypes)
            assertEquals("A", issue.javaClassFqName)
        }
    }

    @Test
    fun testInternalExposedNormal() {
        runWithCompilerEnv("resources@internalExposed/normal") { env ->
            val processor = InternalExposedProcessor()
            runProcessorOnAllJavaFiles(env, this, processor)
            val issues = collectIssues<JavaExtendOrImplInternalKotlinIssue>(this)
            assertEquals(3, issues.size)
            assertArrayEquals(
                arrayOf(
                    "internaltest.java.UseInternalInJava0",
                    "internaltest.kotlin.InternalOpenClassInKotlin"
                ),
                issues.firstOrNull {
                    it.affectedFiles.find { f -> f.endsWith("UseInternalInJava0.java") } != null
                }?.let {
                    arrayOf(it.javaClassFqName, it.exposedTypes.single())
                }
            )
            assertArrayEquals(
                arrayOf(
                    "internaltest.java.UseInternalInJava2",
                    "internaltest.kotlin.InternalInterfaceInKotlin"
                ),
                issues.firstOrNull {
                    it.affectedFiles.find { f -> f.endsWith("UseInternalInJava2.java") } != null
                }?.let {
                    arrayOf(it.javaClassFqName, it.exposedTypes.single())
                }
            )
        }
    }

    private fun assertJavaParameterInternalKotlinIssue1(issue: JavaParameterInternalKotlinIssue) {
        assertEquals("func", issue.javaMethodName)
        assertEquals("KtInternal", issue.kotlinClassNames.single().single())
        assertEquals(0, issue.parameterIndices.single())
        assertEquals(2, issue.startLine)
    }

    private fun assertJavaParameterInternalKotlinIssue2(issue: JavaParameterInternalKotlinIssue) {
        assertEquals("func2", issue.javaMethodName)
        assertEquals(2, issue.kotlinClassNames.size)
        assertEquals("KtInternal", issue.kotlinClassNames[0].single())
        assertEquals("KtInternal2", issue.kotlinClassNames[1].single())
        assertEquals(2, issue.parameterIndices.size)
        assertEquals(1, issue.parameterIndices[0])
        assertEquals(3, issue.parameterIndices[1])
        assertEquals(8, issue.startLine)
    }

    @Test
    fun testJavaParameterInternalKotlinNestedGeneric() {
        runWithCompilerEnv("resources@javaParameterInternalKotlin/nestedGeneric") { env ->
            val processor = InternalExposedProcessor()
            runProcessorOnAllJavaFiles(env, this, processor)
            val issues = collectIssues<JavaParameterInternalKotlinIssue>(this)
            assertEquals(2, issues.size)
            assertTrue(issues.all { it.affectedFiles.size == 2 && it.javaClassFqName == "JavaClass" })
            val issue0 = issues[0]
            val issue1 = issues[1]
            if (issue0.javaMethodName == "func") {
                assertJavaParameterInternalKotlinIssue1(issue0)
                assertJavaParameterInternalKotlinIssue2(issue1)
            } else {
                assertJavaParameterInternalKotlinIssue1(issue1)
                assertJavaParameterInternalKotlinIssue2(issue0)
            }
        }
    }

    @Test
    fun testJavaParameterInternalKotlinGeneric() {
        runWithCompilerEnv("resources@javaParameterInternalKotlin/generic") { env ->
            val processor = InternalExposedProcessor()
            runProcessorOnAllJavaFiles(env, this, processor)
            val issues = collectIssues<JavaParameterInternalKotlinIssue>(this)
            assertEquals(2, issues.size)
            assertTrue(issues.all { it.affectedFiles.size == 2 && it.javaClassFqName == "JavaClass" })
            val issue0 = issues[0]
            val issue1 = issues[1]
            if (issue0.javaMethodName == "func") {
                assertJavaParameterInternalKotlinIssue1(issue0)
                assertJavaParameterInternalKotlinIssue2(issue1)
            } else {
                assertJavaParameterInternalKotlinIssue1(issue1)
                assertJavaParameterInternalKotlinIssue2(issue0)
            }
        }
    }

    @Test
    fun testJavaParameterInternalKotlinNormal() {
        runWithCompilerEnv("resources@javaParameterInternalKotlin/normal") { env ->
            val processor = InternalExposedProcessor()
            runProcessorOnAllJavaFiles(env, this, processor)
            val issues = collectIssues<JavaParameterInternalKotlinIssue>(this)
            assertEquals(2, issues.size)
            assertTrue(issues.all { it.affectedFiles.size == 2 && it.javaClassFqName == "JavaClass" })
            val issue0 = issues[0]
            val issue1 = issues[1]
            if (issue0.javaMethodName == "func") {
                assertJavaParameterInternalKotlinIssue1(issue0)
                assertJavaParameterInternalKotlinIssue2(issue1)
            } else {
                assertJavaParameterInternalKotlinIssue1(issue1)
                assertJavaParameterInternalKotlinIssue2(issue0)
            }
        }
    }

    @Test
    fun testJavaReturnInternalKotlinGeneric() {
        runWithCompilerEnv("resources@javaReturnInternalKotlin/generic") { env ->
            val processor = InternalExposedProcessor()
            runProcessorOnAllJavaFiles(env, this, processor)
            val issues = collectIssues<JavaReturnInternalKotlinIssue>(this)
            val issue = issues.single()
            assertEquals(hashSetOf("KtInternal"), issue.kotlinClassFqNames)
            assertEquals("func", issue.javaMethodName)
            assertEquals("JavaReturn", issue.javaClassFqName)
            assertEquals(2, issue.startLine)
        }
    }

    @Test
    fun testJavaReturnInternalKotlinNormal() {
        runWithCompilerEnv("resources@javaReturnInternalKotlin/normal") { env ->
            val processor = InternalExposedProcessor()
            runProcessorOnAllJavaFiles(env, this, processor)
            val issues = collectIssues<JavaReturnInternalKotlinIssue>(this)
            val issue = issues.single()
            assertEquals(hashSetOf("KtInternal"), issue.kotlinClassFqNames)
            assertEquals("func", issue.javaMethodName)
            assertEquals("JavaReturn", issue.javaClassFqName)
            assertEquals(2, issue.startLine)
        }
    }

    @Test
    fun testNonJVMFieldCompanionValue() {
        runWithCompilerEnv("resources@nonJvmFieldCompanionValue") { env ->
            val processor = NonJVMFieldCompanionValueProcessor()
            runProcessorOnAllKtFiles(env, this, processor)
            val issues = collectIssues<NonJVMFieldCompanionValueIssue>(this)
            assertEquals(1, issues.size)
            assertArrayEquals(
                arrayOf<Any?>("nonJvmFieldCompanionValue.Test.Companion.strts", 6),
                issues.firstOrNull()?.let {
                    arrayOf<Any?>(it.propertyName, it.startLine)
                }
            )
        }
    }

    @Test
    fun testNonJVMStaticCompanionFunction() {
        runWithCompilerEnv("resources@nonJVMStaticCompanionFunction") { env ->
            val processor = NonJVMStaticCompanionFunctionProcessor()
            runProcessorOnAllKtFiles(env, this, processor)
            val issues = collectIssues<NonJVMStaticCompanionFunctionIssue>(this)
            assertEquals(1, issues.size)
            assertArrayEquals(
                arrayOf<Any?>("nonJVMStaticCompanionFunction.Test.Companion.test()", 5),
                issues.firstOrNull()?.let {
                    arrayOf<Any?>(it.functionSignature, it.startLine)
                }
            )
        }
    }

    @Test
    fun testProvideImmutableCollection() {
        runWithCompilerEnv("resources@provideImmutableCollection") { env ->
            val processor = ProvideImmutableCollectionProcessor()
            runProcessorOnAllJavaFiles(env, this, processor)
            val issues = collectIssues<ProvideImmutableCollectionIssue>(this)
            assertEquals(1, issues.size)
            assertArrayEquals(
                arrayOf<Any?>(
                    "provideImmutableCollection.kotlin.pInKotlin",
                    "provideImmutableCollection.java.UseInJava",
                    true
                ),
                issues.firstOrNull()?.let {
                    arrayOf<Any?>(it.providerKtElementFqName, it.useJavaClassFqName, it.isFunction)
                }
            )
        }
    }

    @Test
    fun testUncertainNullablePlatformType() {
        runWithCompilerEnv("resources@unclearPlatformType") { env ->
            val processor = UncertainNullablePlatformTypeProcessor()
            runProcessorOnAllKtFiles(env, this, processor)
            val propertyPlatformType = collectIssues<UncertainNullablePlatformTypeInPropertyIssue>(this)
            val expressionPlatformType = collectIssues<UncertainNullablePlatformExpressionUsageIssue>(this)
            val callerPlatformType = collectIssues<UncertainNullablePlatformCallerIssue>(this)
            assertEquals(5, propertyPlatformType.size)
            assertEquals(2, expressionPlatformType.size)
            assertEquals(2, callerPlatformType.size)
        }
    }

    @Test
    fun testUncertainNullablePlatformCaller() {
        runWithCompilerEnv("resources@unclearPlantformCaller") { env ->
            val processor = UncertainNullablePlatformTypeProcessor()
            runProcessorOnAllKtFiles(env, this, processor)
            val callerPlatformType = collectIssues<UncertainNullablePlatformCallerIssue>(this)
            assertEquals(2, callerPlatformType.size)
            assertEquals(hashSetOf(5, 6), callerPlatformType.map { it.startLine }.toSet())
        }
    }

    @Test
    fun testNullablePassedToPlatformTypeParam() {
        runWithCompilerEnv("resources@nullablePassedToPlatformTypeParam") { env ->
            val processor = UncertainNullablePlatformTypeProcessor()
            runProcessorOnAllKtFiles(env, this, processor)
            val issues = collectIssues<NullablePassedToPlatformParamIssue>(this)
            assertEquals(2, issues.size)
            val sorted = issues.sortedBy { it.startLine }
            val issue = sorted[0]
            assertEquals(4, issue.startLine)
            assertEquals(9, issue.calledFunctionStartLine)
            assertEquals("func1", issue.calledFunctionName)
            val issue1 = sorted[1]
            assertEquals(5, issue1.startLine)
            assertEquals(13, issue1.calledFunctionStartLine)
            assertEquals("func2", issue1.calledFunctionName)
        }
    }

    @Test
    fun testNonNullAssertionOnPlatformType() {
        runWithCompilerEnv("resources@nonnullAssertionOnPlatformType") { env ->
            val processor = UncertainNullablePlatformTypeProcessor()
            runProcessorOnAllKtFiles(env, this, processor)
            val issues = collectIssues<NonNullAssertionOnPlatformTypeIssue>(this)
            val issue = issues.single()
            assertEquals(2, issue.startLine)
            assertEquals("A.func()!!", issue.content)
        }
    }

    @Test
    fun testNonNullAssertionOnNullableType() {
        runWithCompilerEnv("resources@nonnullAssertionOnNullableType") { env ->
            confidenceLevel = ConfidenceLevel.EXTREMELY_LOW
            val processor = UncertainNullablePlatformTypeProcessor()
            runProcessorOnAllKtFiles(env, this, processor)
            val issue = collectIssues<NonNullAssertionOnNullableTypeIssue>(this).single()
            assertEquals(0, collectIssues<NonNullAssertionOnPlatformTypeIssue>(this).size)
            assertEquals(2, issue.startLine)
            assertEquals("A.func()!!", issue.content)
        }
    }
}