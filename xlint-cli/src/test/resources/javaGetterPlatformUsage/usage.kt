package javaGetterPlatformUsage

import java.net.URI

private fun consume(value: String) {}

class UrlState(uri: URI) {
    var scheme: String = ""
    init {
        scheme = uri.scheme // synthetic getter: assignment
    }
}

fun getterInitializer(bean: GetterBean) {
    val text: String = bean.text // synthetic getter: initializer
}

fun getterArgument(bean: GetterBean) {
    consume(bean.text) // synthetic getter: argument
}

fun getterReturn(bean: GetterBean): String = bean.text // synthetic getter: return

fun explicitGetter(uri: URI): String = uri.getScheme() // existing method call

fun safeUses(bean: GetterBean, uri: URI) {
    val nullable: String? = bean.text
    val inferred = bean.text
    val fallback: String = bean.text ?: ""
    val scheme = uri.scheme
    if (scheme != null) consume(scheme)
    uri.scheme?.let { consume(it) }
    val kotlinProperty = "non-null".length
    val parenthesizedFallback: String = ((bean.text)) ?: ""
    val definitelyNonNull: String = bean.nonNullText
    val primitive: Int = bean.count
}
