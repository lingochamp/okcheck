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

import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec

class OkKtlintTask extends JavaExec {

    OkKtlintTask() {
        setDescription("Check Kotlin code style.")
        setGroup("verification")
        setMain("com.github.shyiko.ktlint.Main")
        classpath(project.configurations.ktlint)
        args("src/**/*.kt")

        doFirst {
            println("OkCheck:${project.name} runing OkKtlintTask")
        }
    }

    static String NAME = "okKtlint"

    static void addTask(Project project) {
        project.configure(project) {
            project.configurations {
                ktlint
            }

            dependencies {
                ktlint "com.github.shyiko:ktlint:0.11.1"
            }
        }

        project.task(NAME, type: OkKtlintTask)
        project.task("okKtFormat", type: OkKtlintFormat)

        project.afterEvaluate {
            project.tasks.findByName('check')?.dependsOn(NAME)
        }
    }
}

class OkKtlintFormat extends JavaExec {
    OkKtlintFormat() {
        setDescription("Fix Kotlin code style deviations.")
        setMain("com.github.shyiko.ktlint.Main")
        classpath(project.configurations.ktlint)
        args("-F", "src/**/*.kt")
        setGroup("formatting")
    }
}
