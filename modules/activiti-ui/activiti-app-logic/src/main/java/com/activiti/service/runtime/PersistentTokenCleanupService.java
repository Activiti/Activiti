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

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.activiti.domain.idm.PersistentToken;
import com.activiti.repository.idm.PersistentTokenRepository;

/**
 * We store {@link PersistentToken} objects for users for each device, browser, etc.
 * Also, tokens can be refreshed, resulting in a new row in the database.
 * 
 * To avoid the {@link PersistentToken} table to become too big, this service
 * comes along ones in a while (configurable) to clean up old tokes (ie its data > max age date). 
 * 
 * @author Joram Barrez
 */
@Service
public class PersistentTokenCleanupService {
	
	private static final Logger logger = LoggerFactory.getLogger(PersistentTokenCleanupService.class);
	
	@Autowired
	protected Environment environment;
	
	@Autowired
	private PersistentTokenRepository persistentTokenRepository;
	
	@Transactional
	@Scheduled(cron="${security.cookie.database-removal.cronExpression:0 0 1 * * ?}") // Default 01:00
	public void deleteObsoletePersistentTokens() {
		long maxAge = getTokenMaxAge();
		long now = new Date().getTime();
		Date maxDate = new Date(now - maxAge);
		Long deletedTokens = persistentTokenRepository.deleteByTokenDateBefore(maxDate);
		if (deletedTokens != null) {
			logger.info("Removed " + deletedTokens + " obsolete persisted tokens");
		}
	}
	
	protected long getTokenMaxAge() {
		Integer tokenMaxAgeSeconds = environment.getProperty("security.cookie.database-removal.max-age", Integer.class);
		if (tokenMaxAgeSeconds == null) {
			tokenMaxAgeSeconds = environment.getProperty("security.cookie.max-age", Integer.class, 2678400); // Default 31 days
		}
		return (tokenMaxAgeSeconds.longValue() * 1000L) + 1;
	}

}
