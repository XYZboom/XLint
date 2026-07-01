package io.github.xyzboom.xlint

import com.github.tnoalex.issues.ConfidenceLevel
import com.github.tnoalex.issues.Issue
import com.github.tnoalex.statistics.Statistics
import org.jetbrains.kotlin.analysis.api.KaSession

class XLintContext(
    override var session: KaSession,
    override var confidenceLevel: ConfidenceLevel = ConfidenceLevel.DEFAULT
) : IContext {
    override val issues = HashSet<Issue>()
    override val stats = ArrayList<Statistics>()

    inline fun <R> analyze(action: KaSession.() -> R): R {
        return session.action()
    }

    override fun reportIssue(issue: Issue) {
        // check confidence level that needed
        if (confidenceLevel <= issue.confidenceLevel) {
            // Some processors need to update the details in the issue during process,
            // so remove the old issue first
            issues.remove(issue)
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
}