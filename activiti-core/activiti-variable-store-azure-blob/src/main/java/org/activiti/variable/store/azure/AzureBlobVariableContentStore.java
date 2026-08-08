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

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobStorageException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.UUID;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.variable.store.VariableContentStore;

/**
 * Stores variable content in Azure Blob Storage.
 * contentId = blob name: {blobPrefix}/{processInstanceId}/{variableName}/{uuid}
 */
public class AzureBlobVariableContentStore implements VariableContentStore {

    public static final String STORE_NAME = "azure-blob";

    private final BlobContainerClient containerClient;
    private final String blobPrefix;

    public AzureBlobVariableContentStore(BlobContainerClient containerClient, String blobPrefix) {
        this.containerClient = containerClient;
        this.blobPrefix = blobPrefix;
    }

    @Override
    public String getStoreName() {
        return STORE_NAME;
    }

    @Override
    public String store(String variableName, String processInstanceId, byte[] bytes) {
        if (bytes == null) {
            bytes = new byte[0];
        }
        String pid = processInstanceId != null ? processInstanceId : "no-proc-inst";
        String safeName = variableName.replaceAll("[^a-zA-Z0-9_\-]", "_");
        String blobName = (blobPrefix != null && !blobPrefix.isEmpty() ? blobPrefix + "/" : "")
            + pid + "/" + safeName + "/" + UUID.randomUUID();
        try {
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            blobClient.upload(new ByteArrayInputStream(bytes), bytes.length, true);
        } catch (Exception e) {
            throw new ActivitiException("Failed to store variable '" + variableName + "' to Azure Blob", e);
        }
        return blobName;
    }

    @Override
    public byte[] load(String contentId) {
        try {
            BlobClient blobClient = containerClient.getBlobClient(contentId);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            blobClient.downloadStream(baos);
            return baos.toByteArray();
        } catch (BlobStorageException e) {
            if (e.getStatusCode() == 404) {
                return null;
            }
            throw new ActivitiException("Failed to load variable content from Azure Blob: " + contentId, e);
        } catch (Exception e) {
            throw new ActivitiException("Failed to load variable content from Azure Blob: " + contentId, e);
        }
    }

    @Override
    public void delete(String contentId) {
        if (contentId == null) {
            return;
        }
        try {
            BlobClient blobClient = containerClient.getBlobClient(contentId);
            blobClient.deleteIfExists();
        } catch (Exception e) {
            throw new ActivitiException("Failed to delete variable content from Azure Blob: " + contentId, e);
        }
    }
}
