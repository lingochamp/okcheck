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
import com.liulishuo.okcheck.util.GitUtil
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

class OkCheckPlugin implements Plugin<Project> {

    private static final TASK_NAME = "okcheck"

    private boolean pointCurrentTask = false
    private boolean isRequireOkcheck = false
    private boolean isNeedDiffAllProject = false

    private OkCheckExtension okCheckExtension

    @Override
    void apply(Project project) {
        okCheckExtension = project.extensions.create("okcheck", OkCheckExtension, project)

        // clean okcheck diff
        if (project == project.rootProject) CleanOkCheckDiffTask.addTask(project)

        // lint->checkstyle->ktlint->pmd->findbugs
        if (project != project.rootProject) {
            // we have to apply those plugin first to handle its extension.
            project.configure(project) {
                apply plugin: 'checkstyle'
                apply plugin: 'pmd'
                apply plugin: 'findbugs'
            }

            project.afterEvaluate {
                if (okCheckExtension.enableCheckstyle) OkCheckStyleTask.addTask(project, okCheckExtension.destination)
                if (okCheckExtension.enablePmd) OkPmdTask.addTask(project, okCheckExtension.destination)
                if (okCheckExtension.enableFindbugs) OkFindbugsTask.addTask(project, okCheckExtension.destination)
                if (okCheckExtension.enableKtlint) OkKtlintTask.addTask(project)
            }
        }

        // diff okcheck
        String branchName = GitUtil.currentBranchName()
        if (branchName == null || branchName.length() <= 0) {
            println("OkCheck: this is not on the valid okcheck env, okcheck must running on the git repo!")
            return
        }

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
                        OkCheckTask.addValidTask(project, changedModuleList, okCheckExtension)
                    } else if (pointCurrentTask) {
                        project.getLogger().warn("OkCheck: NO CHANGED CODE FOUND FOR ${project.name}")
                        OkCheckTask.addMockTask(project)
                    }
                } else if (pointCurrentTask) {
                    if (!isChangedModule) {
                        project.getLogger().warn("OkCheck: NO CHANGED CODE FOUND FOR ${project.name}")
                        OkCheckTask.addMockTask(project)
                    } else {
                        OkCheckTask.addValidTask(project, changedModuleList, okCheckExtension)
                    }
                } else if (isRequireOkcheck) {
                    if (isChangedModule) {
                        project.getLogger().warn("OkCheck: if you want to exec okcheck on this module please run "
                                + "command okcheck instead of just for target module")
                    }
                }
            }
        }
    }

    private static void setupOkCheck(Project project) {
        if (project != project.rootProject) throw new IllegalAccessException("only can invoke by the root project!")

        boolean isRequireOkCheck = isRequireOkCheck(project)
        final List<String> changedModuleList = new ArrayList<>()

        if (project.hasProperty("ignoreOkcheckDiff")) {
            if (isRequireOkCheck) println("OkCheck: ignore okcheck diff means every module is free to okcheck!")
            changedModuleList.addAll(ChangeModule.getAllModuleList(project))
        } else if (isFirstBlood(project)) {
            if (isRequireOkCheck) println("OkCheck: First blood means every module is free to okcheck!")
            changedModuleList.addAll(ChangeModule.getAllModuleList(project))
        } else {
            ChangeFile changeFile = new ChangeFile(project.rootProject.name)

            List<String> changeFilePathList = changeFile.getChangeFilePathList()
            if (isRequireOkCheck) {
                println "COMMIT ID BACKUP PATH: ${changeFile.backupPath}"

                println "CHANGE FLIES:"
                changeFilePathList.forEach {
                    println "       $it"
                }
            }
            List<String> changedCodeFilePathList = new ArrayList<>()
            changeFilePathList.forEach {
                if (it.endsWith(".java") || it.endsWith(".groovy") || it.endsWith(".kt") || it.endsWith(".xml")) {
                    changedCodeFilePathList.add(it)
                }
            }


            if (changedCodeFilePathList.isEmpty()) {
                if (isRequireOkCheck) println "NO CHANGED CODE FILE!"
            } else {
                changedModuleList.addAll(ChangeModule.getChangedModuleList(project, changedCodeFilePathList))
                if (isRequireOkCheck) {
                    println "CHANGE MODULES:"
                    changedModuleList.forEach {
                        println "       $it"
                    }
                }
            }
        }


        BuildConfig.saveChangedModuleList(project, changedModuleList)
        BuildConfig.setupPassedModuleFile(project)
    }

    private def setupOkCheckForSubProject(Project project) {
        // point current task
        isRequireOkcheck = isRequireOkCheck(project)
        def taskNames = project.gradle.startParameter.taskNames
        def pointOkCheck = ":" + project.name + ":" + TASK_NAME
        for (int i = 0; i < taskNames.size(); i++) {
            String name = taskNames.get(i)

            if (name == pointOkCheck) {
                pointCurrentTask = true
            }

            if (name == TASK_NAME) {
                isNeedDiffAllProject = true
            }

            if (pointCurrentTask && isNeedDiffAllProject) {
                break
            }
        }

        // ignore non-release build-type to improve speed.
        if (isRequireOkcheck) {
            project.plugins.whenPluginAdded { plugin ->
                if ('com.android.build.gradle.LibraryPlugin' == plugin.class.name) {
                    project.android.variantFilter {
                        if (it.buildType.name != 'release') {
                            it.ignore = true
                        }
                    }
                }
            }

            Map<Project, Set<Task>> map = project.getAllTasks(true)
            Collection<Set<Task>> values = map.values()
            for (Set<Task> taskSet : values) {
                for (Task task : taskSet) {
                    if (task.name.endsWith('DebugUnitTest')) {
                        task.deleteAllActions()
                    }
                }
            }
        }
    }

    private static boolean isRequireOkCheck(Project project) {
        def taskNames = project.gradle.startParameter.taskNames

        boolean isRequireOkcheck = false
        for (int i = 0; i < taskNames.size(); i++) {
            String name = taskNames.get(i)
            if (name.contains(TASK_NAME)) {
                isRequireOkcheck = true
                break
            }
        }
        return isRequireOkcheck
    }

    private static boolean isFirstBlood(Project project) {
        String projectHomePath = "${ChangeFile.okcheckHomePath()}/${project.rootProject.name}"
        // not exist means first time to invoke okcheck(maybe just clean time).
        return !new File(projectHomePath).exists()
    }
}
