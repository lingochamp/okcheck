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

import com.liulishuo.okcheck.util.IncrementFilesHelper
import com.liulishuo.okcheck.util.ResourceUtils
import com.liulishuo.okcheck.util.Util
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.plugins.quality.Checkstyle

class OkCheckStyleTask extends Checkstyle {

    OkCheckStyleTask() {
        setGroup("verification")
        setDescription("Check style with the default set.")

        doLast {
            reports.all { report ->
                def outputFile = report.destination
                if (outputFile.exists() && outputFile.text.contains("<error ")
                        && !ignoreFailures) {
                    throw new GradleException("There were checkstyle warnings! For more info check" +
                            " $outputFile")
                }
            }
        }

        doFirst {
            Util.printLog("${project.name} runing OkCheckstyle")
        }
    }

    static String NAME = "okCheckStyle"
    List<String> changeFiles = IncrementFilesHelper.instance.getModuleChangeFiles(project.name)

    static void addTask(Project project, OkCheckExtension.CheckStyleOptions options) {
        project.configure(project) {
            apply plugin: 'checkstyle'
        }

        project.task(NAME, type: OkCheckStyleTask) {
            project.extensions.checkstyle.with {
                toolVersion = "6.19"

                reports {
                    html.setDestination(options.htmlFile)
                }

                if (options.config != null) {
//                    Util.printLog("Using the custom checkstyle config.")
                    config = options.config
                } else {
//                    Util.printLog("Using the default checkstyle config.")
                    if (project.rootProject.file("suppressions.xml").exists()) {
                        config = ResourceUtils.readTextResource(project, getClass().getClassLoader(), "checkstyle_with_suppression.xml")
                    } else {
                        config = ResourceUtils.readTextResource(project, getClass().getClassLoader(), "checkstyle.xml")
                    }
                }

                if (options.exclude.size() > 0) {
                    exclude options.exclude
                }

                source 'src'

                if (changeFiles.isEmpty()) {
                    include '**/*.java'
                } else {

                    boolean enableCheckStyle = false
                    for(String fileName in changeFiles) {
                        if (fileName.contains(".java")) {
                            include "$fileName"
                            enableCheckStyle = true
                        }
                    }
                    enabled = enableCheckStyle
                }

                exclude '**/gen/**', '**/test/**'
                exclude '**/proto/*.java'
                exclude '**/protobuf/*.java'
                exclude '**/com/google/**/*.java'
                exclude "android/*"
                exclude "androidx/*"
                exclude "com/android/*"

                classpath = project.files()
                reports {
                    xml.enabled = false
                    html.enabled = true
                }
            }
        }

        project.afterEvaluate {
            project.tasks.findByName('check')?.dependsOn(NAME)
        }
    }
}
