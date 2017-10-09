/*
 * Copyright 2017 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.services.query.app.repository;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RestResource;

/**
 * Interface for generic rest resource query operations on a repository for a specific type.
 * 
 * Overrides all entity mutation methods with @RestResource(exposed=false) to disable 
 * HTTP POST, DELETE, or PUT entity resource operations with HTTP error response code 
 * 405 "Method not allowed"
 * 
 */
@NoRepositoryBean
public interface RestResourceQueryRepository<T, I> extends PagingAndSortingRepository<T, I> {

    @Override
    @RestResource(exported = false)
    void deleteById(I id);

    @Override
    @RestResource(exported = false)
    void delete(T entity);

    @Override
    @RestResource(exported = false)
    void deleteAll();

    @Override
    @RestResource(exported = false)
    void deleteAll(Iterable<? extends T> entities);

    @Override
    @RestResource(exported = false)
    <S extends T> S save(S entity);

    @Override
    @RestResource(exported = false)
    <S extends T> Iterable<S> saveAll(Iterable<S> entity);

}