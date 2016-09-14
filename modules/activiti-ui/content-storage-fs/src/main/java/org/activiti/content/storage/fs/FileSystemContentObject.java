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
package org.activiti.content.storage.fs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.activiti.content.storage.api.ContentObject;
import org.activiti.content.storage.exception.ContentStorageException;

/**
 * 
 * {@link ContentObject}, backed by a file. 
 * 
 * @author Frederik Heremans
 */
public class FileSystemContentObject implements ContentObject{

    private File file;
    private InputStream inputStream;
    private String id;
    private Long length;
     
    public FileSystemContentObject(File file, String id, Long length) {
        this.file = file;
        this.id = id;
        this.length = length;
    }
    
    public String getId() {
        return id;
    }

    public long getContentLength() {
        if(length == null) {
            length = file.length();
        }
        return length;
    }

    public InputStream getContent() {
        if(inputStream == null) {
            try {
                inputStream = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                throw new ContentStorageException("Error while opening file stream", e); 
            }
        }
        return inputStream;
    }

}
