/**
 * Activiti app component part of the Activiti project
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.activiti.service.runtime;

import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.activiti.domain.idm.User;
import com.activiti.domain.runtime.Form;
import com.activiti.repository.runtime.FormRepository;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.CacheStats;
import com.google.common.cache.LoadingCache;

/**
 * @author jbarrez
 */
@Service
@Transactional
public class FormStoreServiceImpl implements FormStoreService {
	
	private static final Logger logger = LoggerFactory.getLogger(FormStoreServiceImpl.class);
	
	@Inject
	private FormRepository formRepository;
	
	@Inject
	private Environment environment;
	
	/*
	 * Forms are static once they are created and finalized.
	 * As such, they can be cached aggressively. 
	 */
	private LoadingCache<String, Form> formCache;
	
	@PostConstruct
	protected void initFormCache() {
		Integer formCacheMaxSize = environment.getProperty("cache.forms.max.size", Integer.class);
		 formCache = CacheBuilder.newBuilder()
			.maximumSize(formCacheMaxSize != null ? formCacheMaxSize : 1000)
			.recordStats()
			.build(new CacheLoader<String, Form>() {
				
				public Form load(final String formKey) throws Exception {
				    Long id = Long.valueOf(formKey);
					Form form = formRepository.findOne(id);
					return form;
				}
				
			});
	}
	
	public Form getForm(String formKey) {
		try {
			// The cache is a LoadingCache and will fetch the value itself
			return formCache.get(formKey);
		} catch (ExecutionException e) {
			return null;
		}
	}
	
	@Override
	public Form saveForm(Form form) {
		return formRepository.save(form);
	}
	
	@Override
	public Page<Form> filterForms(User user, String filter, int page, int pageSize) {
	    // TODO: take user into account to filter forms
	    if(StringUtils.isNotBlank(filter)) {
	        return formRepository.findAllByNameLike("%" + filter + "%", new PageRequest(page, pageSize));
	    } else {
	        return formRepository.findAll(new PageRequest(page, pageSize));
	    }
	}
	
	/**
	 * Simply prints out form cache statistics every hour.
	 */
	@Async
	@Scheduled(fixedDelay=3600000L)
	public void generateMetrics() {
		CacheStats cacheStats = formCache.stats();
		logger.info("Form cache statistics: " + cacheStats);
		logger.info("The size of this cache is determined by the 'cache.forms.max.size' property");
	}
	
}
