package com.github.tnoalex

import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.issues.ConfidenceLevel
import com.github.tnoalex.issues.Issue
import com.github.tnoalex.statistics.Statistics
import io.github.xyzboom.xlint.IContext
import org.jetbrains.kotlin.analysis.api.KaSession
import kotlin.reflect.KClass

@Component
class Context : IContext {
    /**
     * Only allowed to set before analyze
     */
    override var confidenceLevel: ConfidenceLevel = ConfidenceLevel.DEFAULT
    override val issues = HashSet<Issue>()
    override val stats = ArrayList<Statistics>()
    override lateinit var session: KaSession

    override fun reportIssue(issue: Issue) {
        // check confidence level that needed
        if (confidenceLevel <= issue.confidenceLevel) {
            issues.add(issue)
        }
    }

    override fun reportIssues(issue: List<Issue>) {
        issue.forEach {
            reportIssue(it)
        }
    }

    override fun resetContext() {
        issues.clear()
        stats.clear()
    }

    override fun reportStatistics(statistics: Statistics) {
        stats.add(statistics)
    }

    fun getIssuesByType(clazz: KClass<out Issue>): List<Issue> {
        return issues.filter { it::class == clazz }
    }
}