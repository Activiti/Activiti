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
package org.activiti.variable.store.azure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AzureBlobVariableContentStoreTest {

    private BlobContainerClient containerClient;
    private BlobClient blobClient;
    private AzureBlobVariableContentStore store;

    @BeforeEach
    void setUp() {
        containerClient = mock(BlobContainerClient.class);
        blobClient = mock(BlobClient.class);
        when(containerClient.getBlobClient(any(String.class))).thenReturn(blobClient);
        store = new AzureBlobVariableContentStore(containerClient, "activiti");
    }

    @Test
    void getStoreName() {
        assertThat(store.getStoreName()).isEqualTo("azure-blob");
    }

    @Test
    void store() {
        String contentId = store.store("myVar", "proc-1", "hello".getBytes());
        assertThat(contentId).startsWith("activiti/proc-1/myVar/");
        verify(blobClient).upload(any(ByteArrayInputStream.class), anyLong(), anyBoolean());
    }

    @Test
    void load() {
        byte[] data = "world".getBytes();
        doAnswer(invocation -> {
            OutputStream outputStream = invocation.getArgument(0);
            outputStream.write(data);
            return null;
        }).when(blobClient).downloadStream(any(OutputStream.class));
        byte[] loaded = store.load("some/blob");
        assertThat(loaded).isEqualTo(data);
    }

    @Test
    void delete() {
        store.delete("some/blob");
        verify(blobClient).deleteIfExists();
    }
}
