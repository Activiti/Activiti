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
package org.activiti.engine.test.db;

import org.apache.ibatis.datasource.pooled.PooledDataSource;

public class IdGeneratorDataSource extends PooledDataSource {

  public IdGeneratorDataSource() {
    setDriver("org.h2.Driver");
    setUrl("jdbc:h2:mem:activiti");
    setUsername("sa");
    setPassword("");
    setPoolMaximumActiveConnections(2);
  }
}
