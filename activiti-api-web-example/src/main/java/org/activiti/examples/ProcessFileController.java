/*
 * Copyright 2018 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.examples;

import java.io.IOException;
import java.io.InputStream;

import org.activiti.runtime.api.ProcessRuntime;
import org.activiti.runtime.api.model.ProcessInstance;
import org.activiti.runtime.api.model.builders.ProcessPayloadBuilder;
import org.apache.commons.io.IOUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController("/documents")
public class ProcessFileController {

    private ProcessRuntime processRuntime;

    public ProcessFileController(ProcessRuntime processRuntime) {
        this.processRuntime = processRuntime;
    }

    @PostMapping
    public String processFile(@RequestParam("file") MultipartFile file, ModelMap modelMap) throws IOException {
        System.out.println("Processing file: " + file.getOriginalFilename());
        String fileContent;
        try (InputStream inputStream = file.getInputStream()) {
            fileContent = IOUtils.toString(inputStream,
                                           "UTF-8");
        }

        ProcessInstance processInstance = processRuntime.start(ProcessPayloadBuilder
                                                                       .start()
                                                                       .withProcessDefinitionKey("categorizeProcess")
                                                                       .withVariable("fileContent",
                                                                                     fileContent)
                                                                       .build());
        String message = ">>> Created Process Instance: " + processInstance;
        System.out.println(message);
        return message;
    }

}
