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
package com.activiti.content.storage.api;

import java.io.InputStream;

import com.activiti.content.storage.exception.ContentNotFoundException;
import com.activiti.content.storage.exception.ContentStorageException;

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
