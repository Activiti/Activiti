package org.activiti5.engine.repository;

import org.activiti5.engine.query.NativeQuery;

/**
 * Allows querying of {@link org.activiti5.engine.repository.Model}s via native (SQL) queries
 * @author Henry Yan(http://www.kafeitu.me)
 */
public interface NativeModelQuery extends NativeQuery<NativeModelQuery, Model> {

}