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

import org.activiti.app.domain.runtime.RelatedContent;
import org.activiti.content.storage.api.ContentStorage;
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
