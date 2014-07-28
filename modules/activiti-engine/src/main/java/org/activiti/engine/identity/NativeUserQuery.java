package org.activiti.engine.identity;

import org.activiti.engine.query.NativeQuery;

/**
 * Allows querying of {@link org.activiti.engine.identity.User}s via native (SQL) queries
 * @author Henry Yan(http://www.kafeitu.me)
 */
public interface NativeUserQuery extends NativeQuery<NativeUserQuery, User> {

}