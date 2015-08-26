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
package com.activiti.content.storage.fs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import com.activiti.content.storage.api.ContentObject;
import com.activiti.content.storage.exception.ContentStorageException;

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
