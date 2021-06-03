/*
 * Copyright (C) 2021 serenesensei
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.serenesensai.gradle.osdetector.facade

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
