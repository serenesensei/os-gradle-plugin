# os-gradle-plugin
A Gradle plugin that detects the OS name and architecture, providing a uniform classifier to be used in the names of native artifacts.

This plugin is made with the help of two other open-source plugins that achieve the same task:
- [google/osdetector-gradle-plugin](https://github.com/google/osdetector-gradle-plugin)
- [trustin/os-maven-plugin](https://github.com/trustin/os-maven-plugin)  

(the former depends on the later)

This plugin is entirely based on gradle, and so it shares the usage documentation of 
[google/osdetector-gradle-plugin](https://github.com/google/osdetector-gradle-plugin)  

If the gradle version on which the build is executed is >= 6.5, gradle configuration cache (experimental) is used to speed-up build times. 

**Note:** The plugin is currently not published anywhere, you can build the artifacts locally by cloning this repository.
