/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.app.service.runtime;

import java.util.Date;

import org.activiti.app.domain.idm.PersistentToken;
import org.activiti.app.repository.idm.PersistentTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
