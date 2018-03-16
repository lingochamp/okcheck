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

package com.liulishuo.okcheck.config

class PmdRuleSet {
    public static String RULE_SET = "<?xml version=\"1.0\"?>\n" +
            "<ruleset xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" name=\"Android Application Rules\"\n" +
            "         xmlns=\"http://pmd.sf.net/ruleset/1.0.0\"\n" +
            "         xsi:noNamespaceSchemaLocation=\"http://pmd.sf.net/ruleset_xml_schema.xsd\"\n" +
            "         xsi:schemaLocation=\"http://pmd.sf.net/ruleset/1.0.0 http://pmd.sf.net/ruleset_xml_schema.xsd\">\n" +
            "\n" +
            "    <description>Custom ruleset for ribot Android application</description>\n" +
            "\n" +
            "    <exclude-pattern>.*/R.java</exclude-pattern>\n" +
            "    <exclude-pattern>.*/gen/.*</exclude-pattern>\n" +
            "\n" +
            "    <rule ref=\"rulesets/java/android.xml\" />\n" +
            "    <rule ref=\"rulesets/java/clone.xml\" />\n" +
            "    <rule ref=\"rulesets/java/finalizers.xml\" />\n" +
            "    <rule ref=\"rulesets/java/imports.xml\">\n" +
            "        <!-- Espresso is designed this way !-->\n" +
            "        <exclude name=\"TooManyStaticImports\" />\n" +
            "    </rule>\n" +
            "    <rule ref=\"rulesets/java/logging-java.xml\">\n" +
            "        <!-- This rule wasn't working properly and given errors in every var call info -->\n" +
            "        <exclude name=\"GuardLogStatementJavaUtil\" />\n" +
            "    </rule>\n" +
            "    <rule ref=\"rulesets/java/braces.xml\">\n" +
            "        <!-- We allow single line if's without braces -->\n" +
            "        <exclude name=\"IfStmtsMustUseBraces\" />\n" +
            "    </rule>\n" +
            "    <rule ref=\"rulesets/java/strings.xml\" >\n" +
            "        <!-- Exclude because causes problems with SQL Strings that usually require duplication -->\n" +
            "        <exclude name=\"AvoidDuplicateLiterals\"/>\n" +
            "    </rule>\n" +
            "    <rule ref=\"rulesets/java/basic.xml\" />\n" +
            "    <rule ref=\"rulesets/java/naming.xml\">\n" +
            "        <exclude name=\"AbstractNaming\" />\n" +
            "        <exclude name=\"LongVariable\" />\n" +
            "        <exclude name=\"ShortMethodName\" />\n" +
            "        <exclude name=\"ShortVariable\" />\n" +
            "        <exclude name=\"ShortClassName\" />\n" +
            "        <exclude name=\"VariableNamingConventions\" />\n" +
            "    </rule>\n" +
            "</ruleset>"
}
