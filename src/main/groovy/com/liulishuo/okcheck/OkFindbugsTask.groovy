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


import com.liulishuo.okcheck.util.DestinationUtil
import com.liulishuo.okcheck.util.ResourceUtils
import com.liulishuo.okcheck.util.Util
import org.gradle.api.Project
import org.gradle.api.plugins.quality.FindBugs

class OkFindbugsTask extends FindBugs {

    OkFindbugsTask() {
        setGroup("verification")

        doFirst {
            Util.printLog("OkCheck:${project.name} runing OkFindbugs")
        }
    }

    static String NAME = "okFindbugs"

    static void addTask(Project project, OkCheckExtension extension) {
        addTask(project, extension, "", "")

        if (!Util.hasAndroidPlugin(project) && !Util.hasLibraryPlugin(project)) return

        def buildTypes = project.android.buildTypes.collect { type -> type.name }
        def productFlavors = project.android.productFlavors.collect { flavor -> flavor.name }

        buildTypes.each { buildType ->
            addTask(project, extension, "", "$buildType")
        }

        productFlavors.each { flavor ->
            buildTypes.each { buildType ->
                if (flavor) {
                    addTask(project, extension, "$flavor", "$buildType")
                }
            }
        }

    }

    static void addTask(Project project, OkCheckExtension extension, String flavor, String buildType) {

        project.task("$NAME${flavor.capitalize()}${buildType.capitalize()}", type: OkFindbugsTask) {
            dependsOn "assemble${flavor.capitalize()}${buildType.capitalize()}"
            if (flavor.length() <= 0 && buildType.length() <= 0) {
                setDescription("Analyzes class with the default set for all variants")
            } else {
                setDescription("Analyzes class with the default set for $flavor$buildType build.")
            }
            project.extensions.findbugs.with {
                reports {
                    html {
                        destination DestinationUtil.getHtmlDest(project, extension.destination, "findbugs")
                    }
                }
                if (extension.findBugsExcludeFilterConfig != null) {
                    setExcludeBugsFilter(extension.findBugsExcludeFilterConfig)
                } else {
                    excludeFilterConfig = ResourceUtils.readTextResource(project, getClass().getClassLoader(), "findbugs-filter.xml")
                }

                if (extension.exclude.size() > 0) {
                    exclude extension.exclude
                }
                classes = project.files("$project.buildDir/intermediates/classes")

                source 'src'
                include '**/*.java'
                exclude '**/gen/**', '**/test/**'
                exclude '**/proto/*.java'
                exclude '**/protobuf/*.java'
                exclude '**/com/google/**/*.java'

                reports {
                    xml.enabled = false
                    html.enabled = true
                }
                classpath = project.files()
            }
        }
    }
}
