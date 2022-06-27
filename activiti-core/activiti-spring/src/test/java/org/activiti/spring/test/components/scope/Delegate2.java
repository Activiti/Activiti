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
package org.activiti.spring.test.components.scope;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import static org.assertj.core.api.Assertions.assertThat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**

 * @since 5, 3
 */

public class Delegate2 implements JavaDelegate {

  private Logger log = LoggerFactory.getLogger(getClass());

  @Autowired
  private StatefulObject statefulObject;

  public void execute(DelegateExecution execution) {

    this.statefulObject.increment();

    assertThat(this.statefulObject).as("the 'scopedCustomer' reference can't be null").isNotNull();
    assertThat(this.statefulObject.getName()).as("the 'scopedCustomer.name' property should be non-null, since it was set in a previous delegate bound to this very thread").isNotNull();
    log.info("the 'uuid' value retrieved from the ScopedCustomer#name property is '{}' in {}", this.statefulObject.getName(), getClass().getName());
  }

}
