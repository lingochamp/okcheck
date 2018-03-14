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

class CheckStyle {

    public static String RULE = "<?xml version=\"1.0\"?>\n" +
            "<!DOCTYPE module PUBLIC\n" +
            "    \"-//Puppy Crawl//DTD Check Configuration 1.3//EN\"\n" +
            "    \"http://checkstyle.sourceforge.net/dtds/configuration_1_3.dtd\">\n" +
            "\n" +
            "<module name=\"Checker\">\n" +
            "    <!--module name=\"NewlineAtEndOfFile\"/-->\n" +
            "    <module name=\"FileLength\"/>\n" +
            "    <module name=\"FileTabCharacter\"/>\n" +
            "\n" +
            "    <!-- Trailing spaces -->\n" +
            "    <module name=\"RegexpSingleline\">\n" +
            "        <property name=\"format\" value=\"\\s+\$\"/>\n" +
            "        <property name=\"message\" value=\"Line has trailing spaces.\"/>\n" +
            "    </module>\n" +
            "\n" +
            "    <!-- Space after 'for' and 'if' -->\n" +
            "    <module name=\"RegexpSingleline\">\n" +
            "        <property name=\"format\" value=\"^\\s*(for|if)[^ ]\\(\"/>\n" +
            "        <property name=\"message\" value=\"Space needed before opening parenthesis.\"/>\n" +
            "    </module>\n" +
            "\n" +
            "    <!-- For each spacing -->\n" +
            "    <module name=\"RegexpSingleline\">\n" +
            "        <property name=\"format\" value=\"^\\s*for \\(.*?([^ ]:|:[^ ])\"/>\n" +
            "        <property name=\"message\" value=\"Space needed around ':' character.\"/>\n" +
            "    </module>\n" +
            "\n" +
            "    <module name=\"SuppressWarningsFilter\" />\n" +
            "\n" +
            "    <module name=\"TreeWalker\">\n" +
            "        <module name=\"LocalFinalVariableName\"/>\n" +
            "        <module name=\"LocalVariableName\"/>\n" +
            "        <module name=\"MemberName\"/>\n" +
            "        <module name=\"MethodName\">\n" +
            "            <property name=\"format\" value=\"^[a-z][a-zA-Z0-9_]*\$\"/>\n" +
            "        </module>\n" +
            "        <module name=\"PackageName\"/>\n" +
            "        <module name=\"ParameterName\"/>\n" +
            "        <module name=\"StaticVariableName\"/>\n" +
            "        <module name=\"TypeName\"/>\n" +
            "\n" +
            "\n" +
            "        <!-- Checks for imports                              -->\n" +
            "        <!-- See http://checkstyle.sf.net/config_import.html -->\n" +
            "        <module name=\"AvoidStarImport\"/>\n" +
            "        <module name=\"IllegalImport\"/>\n" +
            "        <module name=\"RedundantImport\"/>\n" +
            "        <module name=\"UnusedImports\">\n" +
            "            <property name=\"processJavadoc\" value=\"true\"/>\n" +
            "        </module>\n" +
            "\n" +
            "\n" +
            "        <!-- Checks for Size Violations.                    -->\n" +
            "        <!-- See http://checkstyle.sf.net/config_sizes.html -->\n" +
            "        <module name=\"LineLength\">\n" +
            "            <property name=\"max\" value=\"100\"/>\n" +
            "        </module>\n" +
            "        <module name=\"MethodLength\">\n" +
            "            <property name=\"max\" value=\"200\"/>\n" +
            "        </module>\n" +
            "        <!--module name=\"ParameterNumber\"/-->\n" +
            "\n" +
            "\n" +
            "        <!-- Checks for whitespace                               -->\n" +
            "        <!-- See http://checkstyle.sf.net/config_whitespace.html -->\n" +
            "        <module name=\"GenericWhitespace\"/>\n" +
            "        <module name=\"EmptyForIteratorPad\"/>\n" +
            "        <module name=\"MethodParamPad\"/>\n" +
            "        <module name=\"NoWhitespaceAfter\"/>\n" +
            "        <module name=\"NoWhitespaceBefore\"/>\n" +
            "        <module name=\"OperatorWrap\"/>\n" +
            "        <module name=\"ParenPad\"/>\n" +
            "        <module name=\"TypecastParenPad\"/>\n" +
            "        <module name=\"WhitespaceAfter\"/>\n" +
            "        <module name=\"WhitespaceAround\"/>\n" +
            "\n" +
            "\n" +
            "        <!-- Modifier Checks                                    -->\n" +
            "        <!-- See http://checkstyle.sf.net/config_modifiers.html -->\n" +
            "        <module name=\"ModifierOrder\"/>\n" +
            "        <module name=\"RedundantModifier\"/>\n" +
            "\n" +
            "\n" +
            "        <!-- Checks for blocks. You know, those {}'s         -->\n" +
            "        <!-- See http://checkstyle.sf.net/config_blocks.html -->\n" +
            "        <!--module name=\"AvoidNestedBlocks\"/-->\n" +
            "        <!--module name=\"EmptyBlock\"/-->\n" +
            "        <module name=\"LeftCurly\"/>\n" +
            "        <!--module name=\"NeedBraces\"/-->\n" +
            "        <module name=\"RightCurly\"/>\n" +
            "\n" +
            "\n" +
            "        <!-- Checks for common coding problems               -->\n" +
            "        <!-- See http://checkstyle.sf.net/config_coding.html -->\n" +
            "        <!--module name=\"AvoidInlineConditionals\"/-->\n" +
            "        <module name=\"CovariantEquals\"/>\n" +
            "        <module name=\"EmptyStatement\"/>\n" +
            "        <!--<module name=\"EqualsAvoidNull\"/>-->\n" +
            "        <module name=\"EqualsHashCode\"/>\n" +
            "        <!--module name=\"HiddenField\"/-->\n" +
            "        <module name=\"IllegalInstantiation\"/>\n" +
            "        <!--<module name=\"InnerAssignment\"/>-->\n" +
            "        <!--module name=\"MagicNumber\"/-->\n" +
            "        <module name=\"MissingSwitchDefault\"/>\n" +
            "        <!--<module name=\"RedundantThrows\"/>-->\n" +
            "        <module name=\"SimplifyBooleanExpression\"/>\n" +
            "        <module name=\"SimplifyBooleanReturn\"/>\n" +
            "\n" +
            "        <!-- Checks for class design                         -->\n" +
            "        <!-- See http://checkstyle.sf.net/config_design.html -->\n" +
            "        <!--module name=\"DesignForExtension\"/-->\n" +
            "        <!--module name=\"FinalClass\"/-->\n" +
            "        <!--module name=\"HideUtilityClassConstructor\"/-->\n" +
            "        <!--module name=\"InterfaceIsType\"/-->\n" +
            "        <!--module name=\"VisibilityModifier\"/-->\n" +
            "\n" +
            "\n" +
            "        <!-- Miscellaneous other checks.                   -->\n" +
            "        <!-- See http://checkstyle.sf.net/config_misc.html -->\n" +
            "        <!--module name=\"ArrayTypeStyle\"/-->\n" +
            "        <!--module name=\"FinalParameters\"/-->\n" +
            "        <!--module name=\"TodoComment\"/-->\n" +
            "        <module name=\"UpperEll\"/>\n" +
            "\n" +
            "        <module name=\"SuppressWarningsHolder\" />\n" +
            "    </module>\n" +
            "    <module name=\"SuppressionFilter\">\n" +
            "        <property name=\"file\" value=\"suppressions.xml\"/>\n" +
            "    </module>\n" +
            "\n" +
            "</module>"
}
