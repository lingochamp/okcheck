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
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.SourceTask

class OkLint extends SourceTask {

    /**
     * Check whether need to effect the origin lint task to let it incrementally.
     */
    static void inspectLint(Project project, OkCheckExtension.LintOptions options) {
        if (!Util.hasAndroidPlugin(project) && !Util.hasLibraryPlugin(project)) return


        boolean containOkcheck = Util.isCommandContainTask(project, "okcheck")
        boolean containOkLint = Util.isCommandContainTask(project, "okLint")

        boolean incrementalLint = containOkcheck || containOkLint
        addTask(project, options, incrementalLint)

    }

    static void addTask(Project project, OkCheckExtension.LintOptions options, boolean incrementalLint) {
        Util.addTaskWithVariants(project) { flavor, buildType, firstFlavor ->
            addTask(project, options, "${flavor.capitalize()}", "${buildType.capitalize()}", incrementalLint)
        }
    }

    static void addTask(Project project, OkCheckExtension.LintOptions options, String flavor, String buildType, boolean incrementalLint) {

        def outputFile = options.htmlOutput
        FileTree inputFiles = Util.getInputsByType(project, Util.InputType.LINT)

        project.task(getTaskName(flavor, buildType), type: OkLint) {
            dependsOn getOriginTaskName(flavor, buildType)
            inputs.files(inputFiles)
            outputs.file(outputFile)

            setGroup("verification")
            if (flavor.length() <= 0 && buildType.length() <= 0) {
                setDescription("Run lint incremental for all variants")
            } else {
                setDescription("Run lint incremental for $flavor$buildType build.")
            }
        }

        String originTaskName = getOriginTaskName(flavor, buildType)
        Set<Task> originTasks = project.getTasksByName(originTaskName, false)
        Task originTask = null
        if (originTasks != null && originTasks.size() > 0) originTask = originTasks[0]
        if (originTask == null) {
            project.tasks.whenTaskAdded { task ->
                if (task.name == originTaskName) {
                    if (incrementalLint) addIncrementalAndReport(project, task, inputFiles, outputFile)
                }
            }
        } else {
            if (incrementalLint) addIncrementalAndReport(project, originTask, inputFiles, outputFile)
        }

    }

    static String getTaskName(String flavor, String buildType) {
        return "ok${getOriginTaskName(flavor, buildType).capitalize()}"
    }

    private static String getOriginTaskName(String flavor, String buildType) {
        return "lint${flavor.capitalize()}${buildType.capitalize()}"
    }

    private static addIncrementalAndReport(Project project, Task task, FileTree inputFiles, File outputFile) {
        task.inputs.files(inputFiles)
        task.outputs.file(outputFile)

        project.android.lintOptions.htmlReport = true
        project.android.lintOptions.htmlOutput = outputFile
    }
}
