package javaGetterPlatformUsage

import java.net.URI

private fun consume(value: String) {}

class UrlState(uri: URI) {
    var scheme: String = ""
    init {
        scheme = uri.scheme // Positive (smell): Java getter result assigned to non-null String; expect expression-usage warning.
    }
}

fun getterInitializer(bean: GetterBean) {
    val text: String = bean.text // Positive (smell): Java getter result initializes non-null String; expect expression-usage warning.
}

fun getterArgument(bean: GetterBean) {
    consume(bean.text) // Positive (smell): Java getter result passed to non-null parameter; expect expression-usage warning.
}

fun getterReturn(bean: GetterBean): String = bean.text // Positive (smell): Java getter result returned as non-null String; expect expression-usage warning.

fun explicitGetter(uri: URI): String = uri.getScheme() // Positive control (smell): explicit Java getter call; existing expression-usage warning must remain.

fun safeUses(bean: GetterBean, uri: URI) {
    val nullable: String? = bean.text // Negative: nullable target permits null; no expression-usage warning.
    val inferred = bean.text // Negative for expression usage: inferred platform type has no non-null target.
    val fallback: String = bean.text ?: "" // Negative: Elvis supplies a non-null fallback.
    val scheme = uri.scheme // Negative for expression usage: inferred platform type accepts null here.
    if (scheme != null) consume(scheme) // Negative: this same local value is proven non-null.
    uri.scheme?.let { consume(it) } // Negative: safe call skips null and the lambda receives a non-null value.
    val kotlinProperty = "non-null".length // Negative: ordinary non-null Kotlin property, not a Java platform result.
    val parenthesizedFallback: String = ((bean.text)) ?: "" // Negative: parentheses do not remove the Elvis null handling.
    val definitelyNonNull: String = bean.nonNullText // Negative: Java getter has an explicit @NotNull contract.
    val primitive: Int = bean.count // Negative: Java primitive result cannot be null.
}
