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
package org.activiti.engine.impl.variable;

import java.io.Writer;

public class CountingWriter extends Writer {

    private long characterCount = 0;

    public long getCharacterCount() {
        return characterCount;
    }

    @Override
    public void write(int c) {
        characterCount++;
    }

    @Override
    public void write(char[] cbuf, int off, int len) {
        characterCount += len;
    }

    @Override
    public void write(String str, int off, int len) {
        characterCount += len;
    }

    @Override
    public void flush() {
        // No-op: nothing cached in memory
    }

    @Override
    public void close() {
        // No-op: nothing to release
    }
}
