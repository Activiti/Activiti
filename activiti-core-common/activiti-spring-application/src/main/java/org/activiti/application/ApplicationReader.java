/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
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
package org.activiti.application;

import static org.springframework.util.StreamUtils.copyToByteArray;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ApplicationReader {

  private final List<ApplicationEntryDiscovery> applicationEntryDiscoveries;

  public ApplicationReader(List<ApplicationEntryDiscovery> applicationEntryDiscoveries) {
    this.applicationEntryDiscoveries = applicationEntryDiscoveries;
  }

  public ApplicationContent read(InputStream inputStream) {
    try (var zipInputStream = new ZipInputStream(inputStream)) {

      var application = new ApplicationContent();

      ZipEntry zipEntry;

      while ((zipEntry = zipInputStream.getNextEntry()) != null) {

        var currentEntry = zipEntry;

        applicationEntryDiscoveries
          .stream()
          .filter(aed -> aed.filter(currentEntry).test(currentEntry))
          .findFirst()
          .ifPresent(
            applicationEntryDiscovery ->
              application.add(new ApplicationEntry(applicationEntryDiscovery.getEntryType(),
                new FileContent(currentEntry.getName(), readBytes(zipInputStream)))));
      }

      return application;

    } catch (IOException e) {
      throw new ApplicationLoadException("Unable to read zip file", e);
    }
  }

  private byte[] readBytes(ZipInputStream zipInputStream) {
    try {
      return copyToByteArray(zipInputStream);
    } catch (IOException e) {
      throw new ApplicationLoadException("Unable to read zip file", e);
    }
  }
}
