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

import com.liulishuo.okcheck.config.PmdRuleSet
import com.liulishuo.okcheck.util.DestinationUtil
import org.gradle.api.Project
import org.gradle.api.plugins.quality.Pmd

class OkPmdTask extends Pmd {

    OkPmdTask() {
        setGroup("verification")
        setDescription("Runs a set of static code analysis rules on Java source code files and generates a report of " +
                "problems found with the default set.")
        project.extensions.pmd.with {
            ignoreFailures = false
            ruleSetConfig = project.resources.text.fromString(PmdRuleSet.RULE_SET)
            ruleSets = []

            source 'src'
            include '**/*.java'
            exclude '**/gen/**'

            reports {
                xml.enabled = false
                html.enabled = true
            }
        }

        doFirst {
            println("OkCheck:${project.name} runing OkPmd")
        }
    }

    static String NAME = "okPmd"

    static void addTask(Project project, File destination) {
//        project.configure(project) {
//            apply plugin: 'pmd'
//        }
        project.task(NAME, type: OkPmdTask) {
            project.extensions.pmd.with {
                reports {
                    html.setDestination(DestinationUtil.getHtmlDest(project, destination, "pmd"))
                }
            }
        }
        project.afterEvaluate {
            project.tasks.findByName('check')?.dependsOn(NAME)
        }
    }
}
