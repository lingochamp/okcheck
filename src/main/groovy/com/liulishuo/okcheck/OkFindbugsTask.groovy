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
        addTask(project, options, "", "")

        if (!Util.hasAndroidPlugin(project) && !Util.hasLibraryPlugin(project)) return

        def buildTypes = project.android.buildTypes.collect { type -> type.name }
        def productFlavors = project.android.productFlavors.collect { flavor -> flavor.name }

        buildTypes.each { buildType ->
            addTask(project, options, "", "$buildType")
        }

        productFlavors.each { flavor ->
            buildTypes.each { buildType ->
                if (flavor) {
                    addTask(project, options, "$flavor", "$buildType")
                }
            }
        }
    }

    static void addTask(Project project, OkCheckExtension.FindBugsOptions options, String flavor, String buildType) {

        project.task("$NAME${flavor.capitalize()}${buildType.capitalize()}", type: OkFindbugsTask) {
            dependsOn "assemble${flavor.capitalize()}${buildType.capitalize()}"
            if (flavor.length() <= 0 && buildType.length() <= 0) {
                setDescription("Analyzes class with the default set for all variants")
            } else {
                setDescription("Analyzes class with the default set for $flavor$buildType build.")
            }
            project.extensions.findbugs.with {
                reports {
                    xml.enabled = false
                    html {
                        destination options.getHtmlFile()
                        enabled = true
                    }
                }
                if (options.excludeFilterConfig != null) {
                    Util.printLog("Using the custom findbugs exclude filter config.")
                    excludeFilterConfig = options.excludeFilterConfig
                } else {
                    excludeFilterConfig = ResourceUtils.readTextResource(project, getClass().getClassLoader(), "findbugs-filter.xml")
                    Util.printLog("Using the default findbugs exclude filter config.")
                }

                if (options.exclude.size() > 0) {
                    exclude options.exclude
                }

                if (options.effort != null) {
                    Util.printLog("Using special findbugs effort: ${options.effort}")
                    effort = options.effort
                }

                if (options.ignoreFailures) {
                    Util.printLog("Enable ignoreFailures for findbugs")
                    ignoreFailures = true
                }

                source 'src'
                include '**/*.java'
                exclude '**/gen/**', '**/test/**'
                exclude '**/proto/*.java'
                exclude '**/protobuf/*.java'
                exclude '**/com/google/**/*.java'

                classes = project.files("$project.buildDir/intermediates/classes")
                classpath = project.files()
            }
        }
    }
}
