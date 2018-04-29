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

import com.liulishuo.okcheck.config.CheckStyle
import com.liulishuo.okcheck.util.DestinationUtil
import com.liulishuo.okcheck.util.Util
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.plugins.quality.Checkstyle

class OkCheckStyleTask extends Checkstyle {

    OkCheckStyleTask() {
        setGroup("verification")
        setDescription("Check style with the default set.")
        project.extensions.checkstyle.with {
            toolVersion = "8.3"
            if (project.rootProject.file("suppressions.xml").exists()) {
                config = project.resources.text.fromString(CheckStyle.RULE_WITH_SUPPRESSION)
            } else {
                config = project.resources.text.fromString(CheckStyle.RULE)
            }


            source 'src'
            include '**/*.java'
            exclude '**/gen/**', '**/test/**'
            exclude '**/proto/*.java'
            exclude '**/protobuf/*.java'
            exclude '**/com/google/**/*.java'

            classpath = project.files()
            reports {
                xml.enabled = false
                html.enabled = true
            }
        }

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
            Util.printLog("OkCheck:${project.name} runing OkCheckstyle")
        }
    }

    static String NAME = "okCheckStyle"

    static void addTask(Project project, OkCheckExtension extension) {
//        project.configure(project) {
//            apply plugin: 'checkstyle'
//        }

        project.task(NAME, type: OkCheckStyleTask) {
            project.extensions.checkstyle.with {
                reports {
                    html.setDestination(DestinationUtil.getHtmlDest(project, extension.destination, "checkstyle"))
                }

                if (extension.exclude.size() > 0) {
                    exclude extension.exclude
                }
            }
        }

        project.afterEvaluate {
            project.tasks.findByName('check')?.dependsOn(NAME)
        }
    }
}
