/*
 * Copyright (c) 2018 LingoChamp Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.liulishuo.okcheck

import com.liulishuo.okcheck.util.ResourceUtils
import com.liulishuo.okcheck.util.Util
import org.gradle.api.Project
import org.gradle.api.file.FileTree
import org.gradle.api.plugins.quality.FindBugs

class OkFindbugsTask extends FindBugs {

    OkFindbugsTask() {
        setGroup("verification")

        doFirst {
            Util.printLog("${project.name} runing OkFindbugs")
        }
    }

    static String NAME = "okFindbugs"

    static void addTask(Project project, OkCheckExtension.FindBugsOptions options) {
        project.configure(project) {
            apply plugin: 'findbugs'
        }

        if (!Util.hasAndroidPlugin(project) && !Util.hasLibraryPlugin(project)) return

        Util.addTaskWithVariants(project) { flavor, buildType, firstFlavor ->
            addTask(project, options, "$flavor", "$buildType", "$firstFlavor")
        }
    }

    static void addTask(Project project, OkCheckExtension.FindBugsOptions options, String flavor, String buildType, String firstFlavor) {

        String promptTask = "$NAME${flavor.capitalize()}${buildType.capitalize()}Prompt"

        def classFilePaths = ["$project.buildDir/intermediates/classes/$flavor/$buildType",
                              "$project.buildDir/intermediates/javac/$flavor/$buildType",
                              "$project.buildDir/intermediates/javac/${flavor ? flavor + buildType.capitalize() : buildType}"]

        def classFiles = classFilePaths.collect {
            project.fileTree(dir: it, include: "**/*.class")
        }.inject { l, r -> l + r }

        project.task(promptTask) {
            dependsOn "assemble${flavor.capitalize()}${buildType.capitalize()}"
            onlyIf { classFiles.empty }
            doLast {
                println("Could not find class files in any of the following locations:\n$classFilePaths")
            }
        }

        project.task("$NAME${flavor.capitalize()}${buildType.capitalize()}", type: OkFindbugsTask, dependsOn: promptTask) {

            if (flavor.length() <= 0 && buildType.length() <= 0) {
                setDescription("Analyzes class with the default set for all variants")
            } else {
                setDescription("Analyzes class with the default set for ${flavor.capitalize()}${buildType.capitalize()} build.")
            }
            project.extensions.findbugs.with {
                toolVersion = '3.0.1'
                reports {
                    xml {
                        enabled = options.reportXml
                        destination options.getXmlFile()
                    }
                    html {
                        destination options.getHtmlFile()
                        enabled = options.reportHtml
                    }
                }
                if (options.excludeFilterConfig != null) {
//                    Util.printLog("Using the custom findbugs exclude filter config.")
                    excludeFilterConfig = options.excludeFilterConfig
                } else {
                    excludeFilterConfig = ResourceUtils.readTextResource(project, getClass().getClassLoader(), "findbugs-filter.xml")
//                    Util.printLog("Using the default findbugs exclude filter config.")
                }

                if (options.exclude.size() > 0) {
                    exclude options.exclude
                }

                if (options.effort != null) {
//                    Util.printLog("Using special findbugs effort: ${options.effort}")
                    effort = options.effort
                }

                if (options.ignoreFailures) {
//                    Util.printLog("Enable ignoreFailures for findbugs")
                    ignoreFailures = true
                }

                source "src/main/java"
                include Util.getIncludeByType(project, Util.InputType.FIND_BUGS)
                exclude Util.getExclude()

                if ((flavor == null || flavor.isEmpty()) && (firstFlavor != null && !firstFlavor.isEmpty())) {
                    flavor = firstFlavor
                }

                classes = project.files(classFilePaths)
                classpath = project.files()

                onlyIf { !classFiles.empty }

                enabled = options.enabled
            }
        }
    }
}
