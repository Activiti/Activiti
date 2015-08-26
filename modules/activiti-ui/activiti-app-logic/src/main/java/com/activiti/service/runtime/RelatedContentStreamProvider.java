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

import com.activiti.content.storage.api.ContentStorage;
import com.activiti.domain.runtime.RelatedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;

/**
 * Provides access to {@link RelatedContent} raw content streams, regardless of the way it's stored
 * and whether or not the content is linked and streamed from a 3rd party system.
 * 
 * @author Frederik Heremans
 */
@Service
public class RelatedContentStreamProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(RelatedContentStreamProvider.class);

    @Autowired
    private ContentStorage contentStorage;

    public InputStream getContentStream(RelatedContent content) {
        return contentStorage.getContentObject(content.getContentStoreId()).getContent();
    }
}
