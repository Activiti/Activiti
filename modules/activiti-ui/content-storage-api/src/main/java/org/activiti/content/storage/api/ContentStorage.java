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
package org.activiti.content.storage.api;

import java.io.InputStream;

import org.activiti.content.storage.exception.ContentNotFoundException;
import org.activiti.content.storage.exception.ContentStorageException;

/**
 * Storage for reading and writing content.
 * 
 * @author Frederik Heremans
 */
public interface ContentStorage {

    /**
     * @param lengthHint hint about the stream length. If length is unknown, pass in null or a negative number.
     * @return reads the given {@link InputStream} and stores it. Returns a {@link ContentObject} with a unique id generated - which can be
     * used for reading the content again.
     */
    ContentObject createContentObject(InputStream contentStream, Long lengthHint);
    
    /**
     * Update the content with the given id to the content present in the given stream.
     * @param lengthHint hint about the stream length. If length is unknown, pass in null or a negative number.
     * @return Returns a {@link ContentObject} with a unique id generated - which can br used for reading the content again.
     * @throws ContentStorageException When an exception occurred while updating the content and the content
     * is not updated.
     */
    ContentObject updateContentObject(String id, InputStream contentStream, Long lengthHint);
    
    /**
     * @return a {@link ContentObject} with the given id.
     * @throws ContentNotFoundException When the content with the given id does not exist 
     */
    ContentObject getContentObject(String id);
    
    /**
     * Deletes the object the given id.
     * @throws ContentNotFoundException When the content with the given id does not exist 
     * @throws ContentStorageException When an error occurred while deleting the content.
     */
    void deleteContentObject(String id);
}
