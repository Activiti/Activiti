package org.activiti.engine.identity;

import org.activiti.engine.query.NativeQuery;

/**
 * Allows querying of {@link org.activiti.engine.identity.Group}s via native (SQL) queries
 * @author Henry Yan(http://www.kafeitu.me)
 */
public interface NativeGroupQuery extends NativeQuery<NativeGroupQuery, Group> {

}