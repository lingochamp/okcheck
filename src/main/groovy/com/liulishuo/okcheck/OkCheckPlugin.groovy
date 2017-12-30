/*
 * Copyright (c) 2017 LingoChamp Inc.
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

import com.liulishuo.okcheck.util.BuildConfig
import com.liulishuo.okcheck.util.ChangeFile
import com.liulishuo.okcheck.util.ChangeModule
import org.gradle.api.Plugin
import org.gradle.api.Project

class OkCheckPlugin implements Plugin<Project> {

    private static final TASK_NAME = "okcheck"

    private boolean pointCurrentTask = false
    private boolean isContainOkCheck = false

    @Override
    void apply(Project project) {

        if (project == project.rootProject) {
            // root project
            setupOkCheck(project)

            project.task(TASK_NAME, type: OkCheckTask, overwrite: true)
        } else {
            setupOkCheckForSubproject(project)

            // changed module list
            final List<String> changedModuleList = BuildConfig.getChangedModuleList(project)
            project.afterEvaluate {
                if (pointCurrentTask || changedModuleList.contains(project.name)) {
                    if (pointCurrentTask) {
                        println "okcheck: enable check for ${project.name} because of command point to it"
                    } else {
                        println "okcheck: enable check for ${project.name} because of file changed on it"
                    }
                    project.task(TASK_NAME, type: OkCheckTask, overwrite: true) {
                        dependsOn project.getTasksByName('check', false)
                    }
                }
            }
        }
    }

    private def setupOkCheck(Project project) {
        ChangeFile changeFile = new ChangeFile(project.rootProject.name)

        List<String> changeFilePathList = changeFile.getChangeFilePathList()
        println "COMMIT ID BACKUP PATH: ${changeFile.backupPath}"

        println "CHANGE FLIES:"
        changeFilePathList.forEach {
            println "       $it"
        }
        List<String> changedCodeFilePathList = new ArrayList<>()
        changeFilePathList.forEach {
            if (it.endsWith(".java") || it.endsWith(".groovy") || it.endsWith(".kt") || it.endsWith(".xml")) {
                changedCodeFilePathList.add(it)
            }
        }

        final List<String> changedModuleList = new ArrayList<>()

        if (changedCodeFilePathList.isEmpty()) {
            println "NO CHANGED CODE FILE!"
        } else {
            changedModuleList.addAll(ChangeModule.getChangedModuleList(project, changedCodeFilePathList))
        }

        println "CHANGE MODULES:"
        changedModuleList.forEach {
            println "       $it"
        }

        BuildConfig.saveChangedModuleList(project, changedModuleList)
    }

    private def setupOkCheckForSubproject(Project project) {
        // point current task
        def taskNames = project.gradle.startParameter.taskNames
        def pointOkCheck = ":" + project.name + ":" + TASK_NAME
        for (int i = 0; i < taskNames.size(); i++) {
            String name = taskNames.get(i)
            if (name.contains(TASK_NAME)) {
                isContainOkCheck = true
            }

            if (name == pointOkCheck) {
                pointCurrentTask = true
            }

            if (isContainOkCheck && pointOkCheck) {
                break
            }
        }

        // ignore non-release build-type to improve speed.
        if (isContainOkCheck) {
            project.plugins.whenPluginAdded { plugin ->
                if ('com.android.build.gradle.LibraryPlugin' == plugin.class.name) {
                    project.android.variantFilter {
                        if (it.buildType.name != 'release') {
                            it.ignore = true
                        }
                    }
                }
            }
        }
    }
}
