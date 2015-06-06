package org.activiti5.engine.identity;

import org.activiti5.engine.query.NativeQuery;

/**
 * Allows querying of {@link org.activiti5.engine.identity.User}s via native (SQL) queries
 * @author Henry Yan(http://www.kafeitu.me)
 */
public interface NativeUserQuery extends NativeQuery<NativeUserQuery, User> {

}