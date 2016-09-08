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
package org.activiti.app.conf;

import org.activiti.content.storage.api.ContentStorage;
import org.activiti.content.storage.fs.FileSystemContentStorage;
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
