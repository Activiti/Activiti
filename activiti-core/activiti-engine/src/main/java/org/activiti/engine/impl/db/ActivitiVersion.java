/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
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
package org.activiti.engine.impl.db;

import static java.util.Collections.singletonList;

import java.util.List;

/**
 * This class is used for auto-upgrade purposes.
 *
 * The idea is that instances of this class are put in a sequential order, and that the current version is determined from the ACT_GE_PROPERTY table.
 *
 * Since sometimes in the past, a version is ambiguous (eg. 5.12 => 5.12, 5.12.1, 5.12T) this class act as a wrapper with a smarter matches() method.
 *

 */
public class ActivitiVersion {

  protected String mainVersion;
  protected List<String> alternativeVersionStrings;

  public ActivitiVersion(String mainVersion) {
    this.mainVersion = mainVersion;
    this.alternativeVersionStrings = singletonList(mainVersion);
  }

  public ActivitiVersion(String mainVersion, List<String> alternativeVersionStrings) {
    this.mainVersion = mainVersion;
    this.alternativeVersionStrings = alternativeVersionStrings;
  }

  public String getMainVersion() {
    return mainVersion;
  }

  public boolean matches(String version) {
    if (version.equals(mainVersion)) {
      return true;
    } else if (!alternativeVersionStrings.isEmpty()) {
      return alternativeVersionStrings.contains(version);
    } else {
      return false;
    }
  }

  public boolean equals(Object obj) {
    if (!(obj instanceof ActivitiVersion)) {
      return false;
    }
    ActivitiVersion other = (ActivitiVersion) obj;
    boolean mainVersionEqual = mainVersion.equals(other.mainVersion);
    if (!mainVersionEqual) {
      return false;
    } else {
      if (alternativeVersionStrings != null) {
        return alternativeVersionStrings.equals(other.alternativeVersionStrings);
      } else {
        return other.alternativeVersionStrings == null;
      }
    }
  }

}
