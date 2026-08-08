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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FilesystemVariableContentStoreTest {

    private Path storeRoot;

    @BeforeEach
    void setUp() throws IOException {
        storeRoot = Paths.get("target", "filesystem-variable-store-test", UUID.randomUUID().toString());
        Files.createDirectories(storeRoot);
    }

    @AfterEach
    void tearDown() throws IOException {
        if (storeRoot != null && Files.exists(storeRoot)) {
            try (var paths = Files.walk(storeRoot)) {
                paths.sorted(Comparator.reverseOrder()).forEach(path -> path.toFile().delete());
            }
        }
    }

    @Test
    void storeAndLoad() {
        FilesystemVariableContentStore store = new FilesystemVariableContentStore(storeRoot);
        byte[] bytes = "hello world".getBytes();
        String contentId = store.store("myVar", "proc-1", bytes);
        assertThat(contentId).isNotNull();
        byte[] loaded = store.load(contentId);
        assertThat(loaded).isEqualTo(bytes);
    }

    @Test
    void delete() {
        FilesystemVariableContentStore store = new FilesystemVariableContentStore(storeRoot);
        byte[] bytes = "data".getBytes();
        String contentId = store.store("myVar", "proc-1", bytes);
        store.delete(contentId);
        assertThat(store.load(contentId)).isNull();
    }

    @Test
    void loadMissingReturnsNull() {
        FilesystemVariableContentStore store = new FilesystemVariableContentStore(storeRoot);
        assertThat(store.load("nonexistent/path.bin")).isNull();
    }

    @Test
    void getStoreName() {
        FilesystemVariableContentStore store = new FilesystemVariableContentStore(storeRoot);
        assertThat(store.getStoreName()).isEqualTo("filesystem");
    }
}
