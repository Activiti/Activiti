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

package org.activiti.spring.connector;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.model.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class ConnectorService {

    @Value("${connector.root:connector}")
    private String connectorRoot;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Optional<File[]> retrieveFiles() throws IOException {

        Optional<File[]> connectorFiles = Optional.empty();

        Resource connectorRootPath = new ClassPathResource(connectorRoot);
        if (connectorRootPath.exists()) {
            connectorFiles = Optional.ofNullable(connectorRootPath.getFile().listFiles(new FilenameFilter() {

                                                     @Override
                                                     public boolean accept(File dir,
                                                                           String name) {
                                                         if (name.toLowerCase().endsWith(".json")) {
                                                             return true;
                                                         } else {
                                                             return false;
                                                         }
                                                     }
                                                 })
            );
        }
        return connectorFiles;
    }

    private Connector save(File file) throws IOException {
        return objectMapper.readValue(file,
                                      Connector.class);
    }

    public List<Connector> get() throws IOException {

        List<Connector> connectors = new ArrayList<>();
        Optional<File[]> files = retrieveFiles();
        if (files.isPresent()) {
            for (File file : files.get()) {
                connectors.add(save(file));
            }
        }
        return connectors;
    }
}

