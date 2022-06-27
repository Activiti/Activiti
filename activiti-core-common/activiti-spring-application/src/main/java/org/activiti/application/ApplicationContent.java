/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.application;

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApplicationContent {

    private Map<String, List<FileContent>> entries = new HashMap<>();

    public void add(ApplicationEntry entry) {
        List<FileContent> fileContents = entries.computeIfAbsent(entry.getType(),
                                                                 k -> new ArrayList<>());
        fileContents.add(entry.getFileContent());
    }

    public List<FileContent> getFileContents(String entryType) {
        return entries.getOrDefault(entryType, emptyList());
    }

}
