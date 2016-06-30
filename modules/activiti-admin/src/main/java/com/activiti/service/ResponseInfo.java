/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.activiti.service;

import org.apache.http.HttpStatus;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author Bassam Al-Sarori
 */
public class ResponseInfo {

    protected int statusCode;
    protected JsonNode content;
    
    public ResponseInfo(int statusCode) {
        this(statusCode, null);
    }
    
    public ResponseInfo(int statusCode, JsonNode content) {
        this.statusCode = statusCode;
        this.content = content;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
    
    public JsonNode getContent() {
        return content;
    }
    
    public boolean isSuccess() {
        return statusCode == HttpStatus.SC_OK;
    }
    
}
