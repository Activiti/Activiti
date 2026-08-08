/*
 * Copyright 2010-2026 Hyland Software, Inc. and its affiliates.
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
package org.activiti.variable.store.filesystem;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.activiti.engine.impl.variable.store.VariableContentStore;

/**
 * Stores variable content as files under a configurable root directory.
 * contentId is a relative path of the form: {processInstanceId}/{variableName}/{uuid}.bin
 */
public class FilesystemVariableContentStore implements VariableContentStore {

    public static final String STORE_NAME = "filesystem";

    private final Path rootPath;

    public FilesystemVariableContentStore(String rootPath) {
        this.rootPath = Paths.get(rootPath);
    }

    public FilesystemVariableContentStore(Path rootPath) {
        this.rootPath = rootPath;
    }

    @Override
    public String getStoreName() {
        return STORE_NAME;
    }

    @Override
    public String store(String variableName, String processInstanceId, byte[] bytes) {
        String pid = processInstanceId != null ? processInstanceId : "no-proc-inst";
        String safeName = variableName.replaceAll("[^a-zA-Z0-9_\-]", "_");
        String relativePath = pid + "/" + safeName + "/" + UUID.randomUUID() + ".bin";
        Path filePath = rootPath.resolve(relativePath);
        try {
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, bytes != null ? bytes : new byte[0]);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store variable '" + variableName + "'", e);
        }
        return relativePath;
    }

    @Override
    public byte[] load(String contentId) {
        Path filePath = rootPath.resolve(contentId);
        if (!Files.exists(filePath)) {
            return null;
        }
        try {
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load content '" + contentId + "'", e);
        }
    }

    @Override
    public void delete(String contentId) {
        if (contentId == null) {
            return;
        }
        try {
            Files.deleteIfExists(rootPath.resolve(contentId));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to delete content '" + contentId + "'", e);
        }
    }

    public Path getRootPath() {
        return rootPath;
    }
}
