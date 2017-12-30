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
    private boolean isNeedDiffAllProject = false

    @Override
    void apply(Project project) {

        if (project == project.rootProject) {
            // root project
            setupOkCheck(project)

            OkCheckTask.addMockTask(project)
        } else {
            setupOkCheckForSubProject(project)

            // changed module list
            final List<String> changedModuleList = BuildConfig.getChangedModuleList(project)
            project.afterEvaluate {
                boolean isChangedModule = changedModuleList.contains(project.name)
                if (isNeedDiffAllProject) {
                    if (isChangedModule) {
                        println "OkCheck: enable check for ${project.name} because of file changed on it"
                        OkCheckTask.addValidTask(project, changedModuleList)
                    } else if (pointCurrentTask) {
                        project.getLogger().warn("OkCheck: NO CHANGED CODE FOUND FOR ${project.name}")
                        OkCheckTask.addMockTask(project)
                    }
                } else if (pointCurrentTask) {
                    if (!isChangedModule) {
                        project.getLogger().warn("OkCheck: NO CHANGED CODE FOUND FOR ${project.name}")
                        OkCheckTask.addMockTask(project)
                    } else {
                        OkCheckTask.addValidTask(project, changedModuleList)
                    }
                } else if (isContainOkCheck) {
                    if (isChangedModule) {
                        project.getLogger().warn("OkCheck: if you want to exec okcheck on this module please run "
                                + "command okcheck instead of just for target module")
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
        BuildConfig.setupPassedModuleFile(project)
    }

    private def setupOkCheckForSubProject(Project project) {
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

            if (name == TASK_NAME) {
                isNeedDiffAllProject = true
            }

            if (isContainOkCheck && pointCurrentTask && isNeedDiffAllProject) {
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
