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
package com.activiti.service.idm;

import com.activiti.domain.idm.PersistentToken;
import com.activiti.domain.idm.User;
import com.activiti.repository.idm.PersistentTokenRepository;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.security.SecureRandom;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author Joram Barrez
 */
@Service
@Transactional
public class PersistentTokenServiceImpl implements PersistentTokenService {
	
	private static final Logger logger = LoggerFactory.getLogger(PersistentTokenServiceImpl.class);

    private static final int DEFAULT_SERIES_LENGTH = 16;

    private static final int DEFAULT_TOKEN_LENGTH = 16;

    private SecureRandom random;

    @Inject
	private Environment environment;
	
	@Inject
	private PersistentTokenRepository persistentTokenRepository;

	// Caching the persistent tokens to avoid hitting the database too often (eg when doing multiple requests at the same time)
	// (This happens a lot, when the page consists of multiple requests)
	private LoadingCache<String, PersistentToken> tokenCache;

    public PersistentTokenServiceImpl() {
        random = new SecureRandom();
    }

    @PostConstruct
	protected void initTokenCache() {
		Long maxSize = environment.getProperty("cache.login-users.max.size",Long.class);
		Long maxAge = environment.getProperty("cache.login-users.max.age",Long.class);
		tokenCache = CacheBuilder
		        .newBuilder()
		        .maximumSize(maxSize != null ? maxSize : 2048)
		        .expireAfterWrite(maxAge != null ? maxAge : 30, TimeUnit.SECONDS)
		        .recordStats()
		        .build(new CacheLoader<String, PersistentToken>() {

			        public PersistentToken load(final String tokenId) throws Exception {
			        	PersistentToken persistentToken = persistentTokenRepository.findOne(tokenId);
			        	if (persistentToken != null) {
			        		return persistentToken;
			        	} else {
			        		throw new PersistentTokenNotFoundException();
			        	}
			        }

		        });
	}
	
	@Override
	public PersistentToken saveAndFlush(PersistentToken persistentToken) {
		return persistentTokenRepository.saveAndFlush(persistentToken);
	}
	
	@Override
	public void delete(PersistentToken persistentToken) {
		tokenCache.invalidate(persistentToken);
		persistentTokenRepository.delete(persistentToken);
	}
	
	@Override
	public PersistentToken getPersistentToken(String tokenId) {
		return getPersistentToken(tokenId, false);
	}
	
	@Override
	public PersistentToken getPersistentToken(String tokenId, boolean invalidateCacheEntry) {
		
		if (invalidateCacheEntry) {
			tokenCache.invalidate(tokenId);
		}
		
		try {
			return tokenCache.get(tokenId);
        } catch (ExecutionException e) {
        	return null;
        } catch (UncheckedExecutionException e) {
        	return null;
        }
	}


    private String generateSeriesData() {
        byte[] newSeries = new byte[DEFAULT_SERIES_LENGTH];
        random.nextBytes(newSeries);
        return new String(Base64.encode(newSeries));
    }

    private String generateTokenData() {
        byte[] newToken = new byte[DEFAULT_TOKEN_LENGTH];
        random.nextBytes(newToken);
        return new String(Base64.encode(newToken));
    }

    @Override
    public PersistentToken createToken(User user, String remoteAddress, String userAgent) {

        PersistentToken token = new PersistentToken();
        token.setSeries(generateSeriesData());
        token.setUser(user);
        token.setUserId(user.getId());
        token.setTokenValue(generateTokenData());
        token.setTokenDate(new Date());
        token.setIpAddress(remoteAddress);
        token.setUserAgent(userAgent);
        try {
            saveAndFlush(token);
            return token;
        } catch (DataAccessException e) {
            logger.error("Failed to save persistent token ", e);
            return token;
        }
    }
	
	// Just helper exception class for handling null values
	private static class PersistentTokenNotFoundException extends RuntimeException {
		
	}

}
