package org.mesleepy.gradle.osdetector.facade

import org.gradle.api.provider.ProviderFactory

/** Provides simple system property operations. (Gradle version < 6.5) */
internal class SimpleSystemPropertyOperations : SystemPropertyOperationProvider {

    override fun getSystemProperty(name: String): String = System.getProperty(name)

    override fun getSystemProperty(name: String, def: String): String = System.getProperty(name, def)

    override fun setSystemProperty(name: String, value: String): String = System.setProperty(name, value)
}

/** Provides system property operations compatible with Gradle configuration cache. (Gradle version >= 6.5) */
internal class ConfigurationTimeSafeSystemPropertyOperations(
    private val providerFactory: ProviderFactory
) : SystemPropertyOperationProvider {

    override fun getSystemProperty(name: String): String? = providerFactory
        .systemProperty(name)
        .forUseAtConfigurationTime()
        .orNull

    override fun getSystemProperty(name: String, def: String): String = providerFactory
        .systemProperty(name)
        .forUseAtConfigurationTime()
        .getOrElse(def)

    // no-op
    override fun setSystemProperty(name: String, value: String): String? = null
}
