package com.github.tnoalex.processor

import com.github.tnoalex.foundation.ApplicationContext
import com.github.tnoalex.foundation.language.LanguageSupportInfo
import io.github.xyzboom.xlint.IContext

interface BaseProcessor : LanguageSupportInfo {
    val context: IContext
        get() = ApplicationContext.getBeanOfType(IContext::class.java).first()
}