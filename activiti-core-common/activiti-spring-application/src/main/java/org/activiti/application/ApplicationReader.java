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

package org.activiti.application;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.springframework.util.StreamUtils;

public class ApplicationReader {

    private List<ApplicationEntryDiscovery> applicationEntryDiscoveries;

    public ApplicationReader(List<ApplicationEntryDiscovery> applicationEntryDiscoveries) {
        this.applicationEntryDiscoveries = applicationEntryDiscoveries;
    }

    public ApplicationContent read(InputStream inputStream) {
        ApplicationContent application = new ApplicationContent();
        try (ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                ZipEntry currentEntry = zipEntry;
                applicationEntryDiscoveries
                        .stream()
                        .filter(applicationEntryDiscovery -> applicationEntryDiscovery.filter(currentEntry).test(currentEntry))
                        .findFirst()
                        .ifPresent(
                                applicationEntryDiscovery ->
                                        application.add(new ApplicationEntry(applicationEntryDiscovery.getEntryType(),
                                                                             new FileContent(currentEntry.getName(),
                                                                                             readBytes(zipInputStream
                                                                                             )))));
            }
        } catch (IOException e) {
            throw new ApplicationLoadException("Unable to read zip file",
                                              e);
        }
        return application;
    }

    private byte[] readBytes(ZipInputStream zipInputStream) {
        try {
            return StreamUtils.copyToByteArray(zipInputStream);
        } catch (IOException e) {
            throw new ApplicationLoadException("Unable to read zip file",
                                              e);
        }
    }
}
