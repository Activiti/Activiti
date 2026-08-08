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
package org.activiti.variable.store.s3;

import java.util.UUID;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.variable.store.VariableContentStore;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * Stores variable content in AWS S3.
 * contentId = {keyPrefix}/{processInstanceId}/{variableName}/{uuid}
 */
public class S3VariableContentStore implements VariableContentStore {

    public static final String STORE_NAME = "s3";

    private final S3Client s3Client;
    private final String bucketName;
    private final String keyPrefix;

    public S3VariableContentStore(S3Client s3Client, String bucketName, String keyPrefix) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.keyPrefix = keyPrefix;
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
        String key = (keyPrefix != null && !keyPrefix.isEmpty() ? keyPrefix + "/" : "")
            + pid + "/" + safeName + "/" + UUID.randomUUID();
        try {
            s3Client.putObject(
                PutObjectRequest.builder().bucket(bucketName).key(key).build(),
                RequestBody.fromBytes(bytes)
            );
        } catch (Exception e) {
            throw new ActivitiException("Failed to store variable '" + variableName + "' to S3", e);
        }
        return key;
    }

    @Override
    public byte[] load(String contentId) {
        try {
            ResponseBytes<GetObjectResponse> response = s3Client.getObject(
                GetObjectRequest.builder().bucket(bucketName).key(contentId).build(),
                ResponseTransformer.toBytes()
            );
            return response.asByteArray();
        } catch (NoSuchKeyException e) {
            return null;
        } catch (Exception e) {
            throw new ActivitiException("Failed to load variable content from S3: " + contentId, e);
        }
    }

    @Override
    public void delete(String contentId) {
        if (contentId == null) {
            return;
        }
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(contentId).build());
        } catch (Exception e) {
            throw new ActivitiException("Failed to delete variable content from S3: " + contentId, e);
        }
    }
}
