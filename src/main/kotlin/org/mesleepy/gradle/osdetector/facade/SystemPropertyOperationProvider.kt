package org.mesleepy.gradle.osdetector.facade

/**
 * Interface exposing system property operations.
 */
interface SystemPropertyOperationProvider {

    /**
     * Gets the system property indicated by the specified name.
     *
     * @param name the name of the system property.
     * @return the string value of the system property, or `null` if there is no
     * property with that key.
     */
    fun getSystemProperty(name: String): String?

    /**
     * Gets the system property indicated by the specified name.
     *
     * @param name the name of the system property.
     * @param def  a default value.
     * @return the string value of the system property, or the default value if there is
     * no property with that key.
     */
    fun getSystemProperty(name: String, def: String): String?

    /**
     * Sets the system property indicated by the specified name.
     *
     * @param name  the name of the system property.
     * @param value the value of the system property.
     * @return the previous value of the system property, or `null` if it did not have one.
     */
    fun setSystemProperty(name: String, value: String): String?
}
