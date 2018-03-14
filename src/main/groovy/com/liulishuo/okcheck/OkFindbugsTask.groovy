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

import com.liulishuo.okcheck.config.FindbugsFilter
import org.gradle.api.Project
import org.gradle.api.plugins.quality.FindBugs

class OkFindbugsTask extends FindBugs {

    OkFindbugsTask() {
        setDescription("Analyzes class with the default set.")
        setGroup("verification")
        project.extensions.findbugs.with {
            ignoreFailures = false
            excludeFilterConfig = project.resources.text.fromString(FindbugsFilter.FILTER)
            effort = "max"
            reportLevel = "high"
            classes = project.files("$project.buildDir/intermediates/classes")

            source 'src'
            include '**/*.java'
            exclude '**/gen/**'

            reports {
                xml.enabled = false
                html.enabled = true
                xml {
                    destination new File(project.buildDir, "reports/findbugs/findbugs.xml")
                    xml.withMessages true
                }
                html {
                    destination new File(project.buildDir, "reports/findbugs/index.html")
                }
            }
            classpath = project.files()
        }

        doFirst {
            println("OkCheck:${project.name} runing OkFindbugs")
        }
    }

    static String NAME = "okFindbugs"

    static void addTask(Project project) {
//        project.configure(project) {
//            apply plugin: 'findbugs'
//        }


        println("OkCheck: find assemble task: ${project.tasks.findByName('assemble')}")
        project.task(NAME, type: OkFindbugsTask) {
            dependsOn "assemble"
        }
        println("OkCheck: add $NAME task done")
//        project.afterEvaluate {
//            project.tasks.findByName('check')?.dependsOn('okFindbugs')
//        }
    }
}
