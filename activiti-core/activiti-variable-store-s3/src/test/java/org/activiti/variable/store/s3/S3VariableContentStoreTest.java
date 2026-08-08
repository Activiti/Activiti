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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

class S3VariableContentStoreTest {

    private S3Client s3;
    private S3VariableContentStore store;

    @BeforeEach
    void setUp() {
        s3 = mock(S3Client.class);
        store = new S3VariableContentStore(s3, "my-bucket", "activiti");
    }

    @Test
    void getStoreName() {
        assertThat(store.getStoreName()).isEqualTo("s3");
    }

    @Test
    void store() {
        when(s3.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .thenReturn(PutObjectResponse.builder().build());
        String contentId = store.store("myVar", "proc-1", "hello".getBytes());
        assertThat(contentId).startsWith("activiti/proc-1/myVar/");
        verify(s3).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void load() {
        byte[] data = "world".getBytes();
        ResponseBytes<GetObjectResponse> response = ResponseBytes.fromByteArray(GetObjectResponse.builder().build(), data);
        when(s3.getObject(any(GetObjectRequest.class), any(ResponseTransformer.class))).thenReturn(response);
        byte[] loaded = store.load("some/key");
        assertThat(loaded).isEqualTo(data);
    }

    @Test
    void loadMissingReturnsNull() {
        when(s3.getObject(any(GetObjectRequest.class), any(ResponseTransformer.class)))
            .thenThrow(NoSuchKeyException.builder().build());
        assertThat(store.load("missing/key")).isNull();
    }

    @Test
    void delete() {
        store.delete("some/key");
        verify(s3).deleteObject(any(DeleteObjectRequest.class));
    }
}
