package org.activiti.engine.impl.interceptor;

import org.activiti.engine.impl.cfg.TransactionPropagation;

/**
 * Configuration settings for the command interceptor chain.
 * 
 * @author Marcus Klimstra (CGI)
 */
public class CommandConfig {

  private boolean contextReusePossible;
  private TransactionPropagation propagation;
  
  public CommandConfig() {
    this.contextReusePossible = false;
    this.propagation = TransactionPropagation.REQUIRED;
  }
  
  public boolean isContextReusePossible() {
    return contextReusePossible;
  }
  
  public CommandConfig setContextReusePossible(boolean contextReusePossible) {
    this.contextReusePossible = contextReusePossible;
    return this;
  }
  
  public TransactionPropagation getTransactionPropagation() {
    return propagation;
  }

  public CommandConfig setTransactionPropagation(TransactionPropagation propagation) {
    this.propagation = propagation;
    return this;
  }

}
