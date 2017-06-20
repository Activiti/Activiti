package org.activiti.engine.identity;

import org.activiti.engine.query.NativeQuery;

/**
 * Allows querying of {@link org.activiti.engine.identity.User}s via native (SQL) queries
 * 

 */
public interface NativeUserQuery extends NativeQuery<NativeUserQuery, User> {

}