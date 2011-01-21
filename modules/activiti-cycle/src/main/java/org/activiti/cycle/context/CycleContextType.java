package org.activiti.cycle.context;

/**
 * Cycle supports different 'types' of contexts. The {@link #REQUEST} context
 * corresponds to a single user request. The {@link #SESSION}-Context
 * corresponds to a user-session and is initialized once per user-Session. The
 * {@link #APPLICATION}-Context is a singleton-scope. When an instance of a
 * {@link #NONE}-scoped component is retrieved, a fresh instance is created for
 * each retrieval.
 * 
 * @author daniel.meyer@camunda.com
 */
public enum CycleContextType {

  REQUEST,

  SESSION,

  APPLICATION,

  NONE;

}
