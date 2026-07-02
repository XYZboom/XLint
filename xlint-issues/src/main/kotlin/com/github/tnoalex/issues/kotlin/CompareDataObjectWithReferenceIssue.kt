package com.github.tnoalex.issues.kotlin

import com.github.tnoalex.AnalysisHierarchyEnum
import com.github.tnoalex.issues.ConfidenceLevel
import com.github.tnoalex.issues.XLintIssueBundle
import com.github.tnoalex.issues.Issue
import com.github.tnoalex.issues.Severity

class CompareDataObjectWithReferenceIssue(
    affectedFile: String,
    content: String?,
    val leftPropertyFqName: String,
    val rightPropertyFqName: String,
    val startLine: Int
) : Issue(
    XLintIssueBundle.message("issue.name.CompareDataObjectWithReferenceIssue"),
    Severity.CODE_SMELL,
    ConfidenceLevel.COMPLETELY_TRUSTWORTHY,
    AnalysisHierarchyEnum.EXPRESSION,
    hashSetOf(affectedFile),
    content
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as CompareDataObjectWithReferenceIssue

        if (leftPropertyFqName != other.leftPropertyFqName) return false
        if (rightPropertyFqName != other.rightPropertyFqName) return false
        if (startLine != other.startLine) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + leftPropertyFqName.hashCode()
        result = 31 * result + rightPropertyFqName.hashCode()
        result = 31 * result + startLine
        return result
    }
}