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
package org.activiti.engine.impl.interceptor;

import org.activiti.engine.impl.cfg.TransactionPropagation;

/**
 * Configuration settings for the command interceptor chain.
 *
 * Instances of this class are immutable, and thus thread- and share-safe.
 *

 */
public class CommandConfig {

  private boolean contextReusePossible;
  private TransactionPropagation propagation;

  public CommandConfig() {
    this.contextReusePossible = true;
    this.propagation = TransactionPropagation.REQUIRED;
  }

  public CommandConfig(boolean contextReusePossible) {
    this.contextReusePossible = contextReusePossible;
    this.propagation = TransactionPropagation.REQUIRED;
  }

  public CommandConfig(boolean contextReusePossible, TransactionPropagation transactionPropagation) {
    this.contextReusePossible = contextReusePossible;
    this.propagation = transactionPropagation;
  }

  protected CommandConfig(CommandConfig commandConfig) {
    this.contextReusePossible = commandConfig.contextReusePossible;
    this.propagation = commandConfig.propagation;
  }

  public boolean isContextReusePossible() {
    return contextReusePossible;
  }

  public TransactionPropagation getTransactionPropagation() {
    return propagation;
  }

  public CommandConfig setContextReusePossible(boolean contextReusePossible) {
    CommandConfig config = new CommandConfig(this);
    config.contextReusePossible = contextReusePossible;
    return config;
  }

  public CommandConfig transactionRequired() {
    CommandConfig config = new CommandConfig(this);
    config.propagation = TransactionPropagation.REQUIRED;
    return config;
  }

  public CommandConfig transactionRequiresNew() {
    CommandConfig config = new CommandConfig();
    config.contextReusePossible = false;
    config.propagation = TransactionPropagation.REQUIRES_NEW;
    return config;
  }

  public CommandConfig transactionNotSupported() {
    CommandConfig config = new CommandConfig();
    config.contextReusePossible = false;
    config.propagation = TransactionPropagation.NOT_SUPPORTED;
    return config;
  }
}
