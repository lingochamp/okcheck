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

class FindbugsFilter {
    public static String FILTER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<FindBugsFilter>\n" +
            "    <Match>\n" +
            "        <Class name=\"~.*\\.R\\\$.*\"/>\n" +
            "    </Match>\n" +
            "    <Match>\n" +
            "        <Class name=\"~.*\\.Manifest\\\$.*\"/>\n" +
            "    </Match>\n" +
            "    <Match>\n" +
            "        <Class name=\"~.*\\.*Test\" />\n" +
            "        <Not>\n" +
            "            <Bug code=\"IJU\" />\n" +
            "        </Not>\n" +
            "    </Match>\n" +
            "\n" +
            "    <Match>\n" +
            "        <Package name=\"com.actionbarsherlock.internal.nineoldandroids.animation\" />\n" +
            "    </Match>\n" +
            "\n" +
            "    <Match>\n" +
            "        <Package name=\"com.actionbarsherlock.view\" />\n" +
            "    </Match>\n" +
            "\n" +
            "    <Match>\n" +
            "        <Package name=\"com.actionbarsherlock.internal.widget\" />\n" +
            "    </Match>\n" +
            "\n" +
            "</FindBugsFilter>"
}
