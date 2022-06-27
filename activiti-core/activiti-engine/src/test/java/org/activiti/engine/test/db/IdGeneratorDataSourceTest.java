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

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.impl.test.ResourceActivitiTestCase;
import org.activiti.engine.test.Deployment;

public class IdGeneratorDataSourceTest extends ResourceActivitiTestCase {

  public IdGeneratorDataSourceTest() {
    super("org/activiti/engine/test/db/IdGeneratorDataSourceTest.activiti.cfg.xml");
  }

  @Deployment
  public void testIdGeneratorDataSource() {
    List<Thread> threads = new ArrayList<Thread>();
    for (int i = 0; i < 20; i++) {
      Thread thread = new Thread() {
        public void run() {
          for (int j = 0; j < 5; j++) {
            runtimeService.startProcessInstanceByKey("idGeneratorDataSource");
          }
        }
      };
      thread.start();
      threads.add(thread);
    }

    for (Thread thread : threads) {
      try {
        thread.join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}
