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

import com.liulishuo.okcheck.util.Util
import org.gradle.api.Project
import org.gradle.testing.jacoco.tasks.JacocoReport

class OkCoverageReport extends JacocoReport {

    OkCoverageReport() {
        setGroup("verification")

        doFirst {
            Util.printLog("$project.name running OkCoverageReport")
        }
    }

    static void addTask(Project project, OkCheckExtension.CoverageReportOptions options) {
        if (!Util.hasAndroidPlugin(project) && !Util.hasLibraryPlugin(project)) return

        project.configure(project) {
            apply plugin: 'jacoco'

            android {
                testOptions {
                    unitTests.all {
                        jacoco {
                            // unitTest jacoco exec
                            includeNoLocationClasses = true
                        }
                    }
                }

                // just for androidTest on the debug variant, because we don't other build type is debug or release
                buildTypes {
                    debug {
                        // androidTest jacoco ec
                        testCoverageEnabled true
                    }
                }
            }
        }

        def buildTypes = project.android.buildTypes.collect { type -> type.name }
        def productFlavors = project.android.productFlavors.collect { flavor -> flavor.name }
        String depTaskName = "create"
        if (productFlavors.size() > 0) {
            depTaskName += "${productFlavors.get(0)}".capitalize()
        }
        if (!buildTypes.contains("Debug")) {
            depTaskName += "${buildTypes.get(0).capitalize()}CoverageReport"
        } else {
            depTaskName += "DebugCoverageReport"
        }

        Util.addTaskWithVariants(project) { flavor, buildType, firstFlavor ->
            String taskName = getTaskName("$flavor", "$buildType")
            String unitTestName = Util.getUnitTestTaskName(project.name, taskName, "$flavor", "$buildType", "$firstFlavor")
            project.task(taskName, type: OkCoverageReport) {
                inputs.files(Util.getAllInputs(project))
                outputs.dir(options.htmlFile.getAbsolutePath())
                dependsOn(unitTestName, depTaskName)
                if (flavor.length() <= 0 && buildType.length() <= 0) {
                    setDescription("Generate HTML, Xml, CSV(default false) reports of Jacoco coverage data for all variants of unit-test and integrate-test")
                } else {
                    setDescription("Generate HTML, Xml, CSV(default false) reports of Jacoco coverage data for ${flavor.capitalize()}${buildType.capitalize()} variants of unit-test and integrate-test.")
                }

                reports {
                    xml {
                        enabled = options.xml.enabled
                        destination = options.xmlFile
                    }
                    html {
                        enabled = options.html.enabled
                        destination = options.htmlFile
                    }
                    csv {
                        enabled = options.csv.enabled
                        destination = options.csvFile
                    }
                }

                def fileFilter = ['**/R.class', '**/R$*.class', '**/BuildConfig.*', '**/Manifest*.*', '**/*Test*.*', 'android/**/*.*']
                def oldTree = project.fileTree(dir: "${project.buildDir}/intermediates/classes/$flavor/$buildType", excludes: fileFilter)
                def newTree = project.fileTree(dir: "${project.buildDir}/intermediates/javac/$flavor/$buildType", excludes: fileFilter)

                sourceDirectories = project.files(["${project.projectDir}/src/main/java", "${project.projectDir}/src/main/kotlin"])
                classDirectories = project.files([oldTree, newTree])
                executionData = project.fileTree(dir: "$project.buildDir", includes: [
                        // <<< testUnitTest
                        "jacoco/testDebugUnitTest.exec",
                        // <<< createCoverageReport
                        "outputs/code-coverage/connected/*coverage.ec"
                ])
            }
        }
    }

    static String getTaskName(String flavor, String buildType) {
        return "okCoverage${flavor.capitalize()}${buildType.capitalize()}Report"
    }
}
