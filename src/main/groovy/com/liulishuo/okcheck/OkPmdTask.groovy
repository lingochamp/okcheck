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
import org.gradle.api.plugins.quality.Pmd

class OkPmdTask extends Pmd {

    OkPmdTask() {
        setGroup("verification")
        setDescription("Runs a set of static code analysis rules on Java source code files and generates a report of " +
                "problems found with the default set.")

        doFirst {
            Util.printLog("${project.name} runing OkPmd")
        }
    }

    static String NAME = "okPmd"

    static void addTask(Project project, OkCheckExtension.PmdOptions options) {
        project.configure(project) {
            apply plugin: 'pmd'
        }
        def inputFiles = project.fileTree(dir: "src", include: "**/*.kt")
        inputFiles += project.fileTree(dir: "src", include: "**/*.java")
        def outputFile = options.htmlFile

        project.task(NAME, type: OkPmdTask) {
            inputs.files(inputFiles)
            outputs.file(outputFile)

            project.extensions.pmd.with {
                reports {
                    html.setDestination(outputFile)
                    html.enabled = true
                    xml.enabled = false
                }
                if (options.ruleSetConfig != null) {
//                    Util.printLog("Using the custom pmd rule set config.")
                    ruleSetConfig = options.ruleSetConfig
                } else {
//                    Util.printLog("Using the default pmd rule set config.")
                    ruleSetConfig = ResourceUtils.readTextResource(project, getClass().getClassLoader(), "pmd-ruleset.xml")
                }

                if (options.exclude.size() > 0) {
                    exclude options.exclude
                }

                ruleSets = []
                source 'src'
                include '**/*.java'
                exclude '**/gen/**', '**/test/**'
                exclude '**/proto/*.java'
                exclude '**/protobuf/*.java'
                exclude '**/com/google/**/*.java'

                if (options.ignoreFailures) {
//                    Util.printLog("Enable ignoreFailures for pmd")
                    ignoreFailures = true
                }

            }
        }
        project.afterEvaluate {
            project.tasks.findByName('check')?.dependsOn(NAME)
        }
    }
}
