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

package org.activiti.engine.test.impl.logger;

import java.util.ArrayList;
import java.util.List;


public abstract class AbstractDebugInfo implements DebugInfo {

  protected List<DebugInfoExecutionTree> executionTrees = new ArrayList<DebugInfoExecutionTree>();

  public List<DebugInfoExecutionTree> getExecutionTrees() {
    return executionTrees;
  }

  public void setExecutionTrees(List<DebugInfoExecutionTree> executionTrees) {
    this.executionTrees = executionTrees;
  }

  public void addExecutionTree(DebugInfoExecutionTree executionTree) {
    executionTrees.add(executionTree);
  }

}
