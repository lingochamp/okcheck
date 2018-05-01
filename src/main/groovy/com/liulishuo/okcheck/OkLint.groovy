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

import com.liulishuo.okcheck.util.Util
import org.gradle.api.DefaultTask
import org.gradle.api.Project

class OkLint extends DefaultTask {

    /**
     * Check whether need to effect the origin lint task to let it incrementally.
     */
    static void inspectLint(Project project, OkCheckExtension.LintOptions options) {
        boolean containOkcheck = Util.isCommandContainTask(project, "okcheck")
        boolean containOkLint = Util.isCommandContainTask(project, "okLint")

        boolean incrementalLint = containOkcheck || containOkLint
        addTask(project, options, incrementalLint)

    }

    static void addTask(Project project, OkCheckExtension.LintOptions options, boolean incrementalLint) {
        addTask(project, options, "", "", incrementalLint)
        def buildTypes = project.android.buildTypes.collect { type -> type.name }
        def productFlavors = project.android.productFlavors.collect { flavor -> flavor.name }
        buildTypes.each { buildType ->
            addTask(project, options, "${buildType.capitalize()}", "", incrementalLint)
        }

        productFlavors.each { flavor ->
            buildTypes.each { buildType ->
                if (flavor) {
                    addTask(project, options, "${flavor.capitalize()}", "${buildType.capitalize()}", incrementalLint)
                }
            }
        }
    }

    static void addTask(Project project, OkCheckExtension.LintOptions options, String buildType, String flavor, boolean incrementalLint) {

        def inputFiles = project.fileTree(dir: "src", include: "**/*.kt")
        inputFiles += project.fileTree(dir: "src", include: "**/*.java")
        inputFiles += project.fileTree(dir: "src", include: "**/*.groovy")
        inputFiles += project.fileTree(dir: "src", include: "**/*.xml")
        def outputFile = options.htmlOutput

        project.tasks.whenTaskAdded { task ->
            if (task.name == getOriginTaskName(flavor, buildType)) {
                if (incrementalLint) {
                    task.inputs.files(inputFiles)
                    task.outputs.file(outputFile)

                    project.android.lintOptions.htmlReport = true
                    project.android.lintOptions.htmlOutput = outputFile
                }

                project.task(getTaskName(flavor, buildType), type: OkLint) {
                    dependsOn task.name
                    inputs.files(inputFiles)
                    outputs.file(outputFile)

                    setGroup("verification")
                    if (flavor.length() <= 0 && buildType.length() <= 0) {
                        setDescription("Run lint incremental for all variants")
                    } else {
                        setDescription("Run lint incremental for $flavor$buildType build.")
                    }
                }
            }
        }
    }

    static String getTaskName(String flavor, String buildType) {
        return "ok${getOriginTaskName(flavor, buildType).capitalize()}"
    }

    private static String getOriginTaskName(String flavor, String buildType) {
        return "lint${flavor.capitalize()}${buildType.capitalize()}"
    }
}
