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

import org.gradle.api.Plugin
import org.gradle.api.Project

class OkCheckPlugin implements Plugin<Project> {

    private static final TASK_NAME = "okcheck"

    @Override
    void apply(Project project) {

        boolean isContainOkCheck = false
        boolean pointCurrentTask = false
        def taskNames = project.gradle.startParameter.taskNames

        def pointOkCheck = ":" + project.name + ":" + TASK_NAME
        for (int i = 0; i < taskNames.size(); i++) {
            String name = taskNames.get(i)
            if (name.endsWith(TASK_NAME)) {
                isContainOkCheck = true
            }

            println pointOkCheck
            if (name == pointOkCheck) {
                println 'point to the current task'
                pointCurrentTask = true
            }

            if (isContainOkCheck && pointCurrentTask) break
        }

        if (isContainOkCheck) {
            project.plugins.whenPluginAdded { plugin ->
                if ('com.android.build.gradle.LibraryPlugin' == plugin.class.name) {
                    project.android.variantFilter {
                        if (it.buildType.name != 'release') {
                            println 'okcheck: on the current version okcheck only check release buildType'
                            it.ignore = true
                        }
                    }
                }
            }
        }

        def effectModule = ''

        File rootDir = project.rootProject.rootDir
        project.afterEvaluate {
            project.allprojects {
                String relativePath = rootDir.toURI().relativize(it.projectDir.toURI()).getPath()
                println "get project and path: ${it.name} with $relativePath"
            }

            if (project.name == effectModule || pointCurrentTask) {
                project.task(TASK_NAME, type: OkCheckTask, overwrite: true) {
                    dependsOn project.getTasksByName('check', false)
                }
            } else {
                project.task(TASK_NAME, type: OkCheckTask, overwrite: true)
            }

        }
    }
}
