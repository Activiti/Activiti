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
package com.activiti.conf;

import com.activiti.content.storage.api.ContentStorage;
import com.activiti.content.storage.fs.FileSystemContentStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.inject.Inject;
import java.io.File;

@Configuration
public class ContentStorageConfiguration {

    private final Logger log = LoggerFactory.getLogger(ContentStorageConfiguration.class);
    
    private static final String PROP_FS_ROOT = "contentstorage.fs.rootFolder";
    private static final String PROP_FS_DEPTH = "contentstorage.fs.depth";
    private static final String PROP_FS_BLOCK_SIZE = "contentstorage.fs.blockSize";
    private static final String PROP_FS_CREATE_ROOT = "contentstorage.fs.createRoot";
    
    private static final Integer DEFAULT_FS_DEPTH = 4;
    private static final Integer DEFAULT_FS_BLOCK_SIZE = 1024;
    
    @Inject
    private Environment env;
    
    /**
     * @return an instance of {@link ContentStorage}. Depending on the configured properties,
     * a different implementation will be created.
     */
    @Bean
    public ContentStorage contentStorage() {
        
        // TODO: separate the configuration of each instance to a class, so this class doesn't depend on any IMPL being
        // on the classpath
        
        String fsRoot = env.getProperty(PROP_FS_ROOT);
        log.info("Using file-system based content storage (" + fsRoot + ")");
        Integer iterationDepth = env.getProperty(PROP_FS_DEPTH, Integer.class, DEFAULT_FS_DEPTH);
        Integer blockSize = env.getProperty(PROP_FS_BLOCK_SIZE, Integer.class, DEFAULT_FS_BLOCK_SIZE);

        File root = new File(fsRoot);
        if(env.getProperty(PROP_FS_CREATE_ROOT, Boolean.class, Boolean.FALSE).booleanValue() && !root.exists()) {
           log.info("Creating content storage root and possible missing parents: " + root.getAbsolutePath());
           root.mkdirs();
        }
        if (root != null && root.exists()) {
           log.info("File system root : " + root.getAbsolutePath());
        }
        return new FileSystemContentStorage(root, blockSize, iterationDepth);
    }
}
