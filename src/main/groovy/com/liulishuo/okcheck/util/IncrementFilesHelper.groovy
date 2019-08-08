package com.liulishuo.okcheck.util
/*
 * Copyright (c) 2019 LingoChamp Inc.
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

class IncrementFilesHelper {
    private List<String> incrementFiles = new ArrayList<>();
    private List<String> changeModules = new ArrayList<>()

    private  IncrementFilesHelper() {}

    static IncrementFilesHelper getInstance() {
        return Holder.INSTANCE
    }

    private static class Holder {
        private static final INSTANCE = new IncrementFilesHelper();
    }

    List<String> getIncrementFiles() {
        return incrementFiles
    }

    void addIncrementFiles(List<String> files) {
        incrementFiles.clear();
        incrementFiles.addAll(files);
    }

    List<String> getChangeModules() {
        return changeModules
    }

    void addChangeModules(List<String> modules) {
        changeModules.clear()
        changeModules.addAll(modules)
    }

    boolean needAddTask(String name) {
        return incrementFiles.size() >0 && incrementFiles.contains(name)
    }
}