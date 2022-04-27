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

package org.activiti.standalone.deploy;

import java.io.InputStream;
import java.util.Scanner;


public class DeploymentCacheTestUtil {

  public static String readTemplateFile(String templateFile) {
    InputStream inputStream = DeploymentCacheTestUtil.class.getResourceAsStream(templateFile);
    Scanner scanner = null;
    try {
      scanner = new Scanner(inputStream, "UTF-8").useDelimiter("\\A");
      if (scanner.hasNext()) {
        return scanner.next();
      }
    } finally {
      if (scanner != null) {
        scanner.close();
      }
    }
    return null;
  }

}
