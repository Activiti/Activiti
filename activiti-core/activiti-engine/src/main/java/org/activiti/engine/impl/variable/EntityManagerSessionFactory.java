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


package org.activiti.engine.impl.variable;

import javax.persistence.EntityManagerFactory;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.interceptor.SessionFactory;


public class EntityManagerSessionFactory implements SessionFactory {

  protected EntityManagerFactory entityManagerFactory;
  protected boolean handleTransactions;
  protected boolean closeEntityManager;

  public EntityManagerSessionFactory(Object entityManagerFactory, boolean handleTransactions, boolean closeEntityManager) {
    if (entityManagerFactory == null) {
      throw new ActivitiIllegalArgumentException("entityManagerFactory is null");
    }
    if (!(entityManagerFactory instanceof EntityManagerFactory)) {
      throw new ActivitiIllegalArgumentException("EntityManagerFactory must implement 'javax.persistence.EntityManagerFactory'");
    }

    this.entityManagerFactory = (EntityManagerFactory) entityManagerFactory;
    this.handleTransactions = handleTransactions;
    this.closeEntityManager = closeEntityManager;
  }

  public Class<?> getSessionType() {
    return EntityManagerSession.class;
  }

  public Session openSession(CommandContext commandContext) {
    return new EntityManagerSessionImpl(entityManagerFactory, handleTransactions, closeEntityManager);
  }

  public EntityManagerFactory getEntityManagerFactory() {
    return entityManagerFactory;
  }
}
