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
package org.activiti.engine.impl.bpmn.helper;

import java.io.Serializable;

/**
 * Contextual data about a caught BPMN error, passed through the output
 * mapping pipeline and optionally stored as a process variable.
 *
 * <p>Implements {@link Serializable} so it can be set directly as a
 * process variable value.</p>
 */
public class ErrorContext implements Serializable {

    private static final long serialVersionUID = 1L;

    private String errorId;
    private String errorCode;
    private String errorName;
    private String sourceId;

    public ErrorContext(String errorId, String errorCode, String errorName) {
        this.errorId = errorId;
        this.errorCode = errorCode;
        this.errorName = errorName;
    }

    public String getErrorId() {
        return errorId;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorName() {
        return errorName;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }
}
