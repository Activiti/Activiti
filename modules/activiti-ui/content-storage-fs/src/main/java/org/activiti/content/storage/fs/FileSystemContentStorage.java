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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

import org.activiti.content.storage.api.ContentObject;
import org.activiti.content.storage.api.ContentStorage;
import org.activiti.content.storage.exception.ContentNotFoundException;
import org.activiti.content.storage.exception.ContentStorageException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Frederik Heremans
 */
public class FileSystemContentStorage implements ContentStorage {

    private static final String TEMP_SUFFIX = "_TEMP";
    private static final String OLD_SUFFIX = "_OLD";
    
    private File rootFolder;
    private File currentLeafFolder;
    private PathConverter converter;
    
    private Object idLock = new Object();
    private int currentIndex = 0;
    
    private static final Logger logger = LoggerFactory.getLogger(FileSystemContentStorage.class);

    /**
     * Maximum number of times the next folder should be attempted to be acquired in a row before
     * failing. 
     */
    private static final int MAX_FOLDER_RETRIES = 50;
    
    /**
     * @param contentFolderRoot root folder where all content will be stored in
     * @param blockSize max size of a single folder's children
     * @param depth depth of the tree created to store content in
     * @see PathConverter
     */
    public FileSystemContentStorage(File contentFolderRoot, int blockSize, int depth) {
        this.rootFolder = contentFolderRoot;
        
        // Create and initialize
        converter = new PathConverter();
        converter.setBlockSize(blockSize);
        converter.setIterationDepth(depth);
    }
    
    public ContentObject createContentObject(InputStream contentStream, Long lengthHint) {
        // Get hold of the next free ID to use
        BigInteger id = fetchNewId();
        File contentFile = new File(rootFolder, converter.getPathForId(id).getPath());
        long length = -1;
        try {
            length = IOUtils.copy(contentStream, new FileOutputStream(contentFile, false));
        } catch (FileNotFoundException e) {
            throw new ContentStorageException("Content file was deleted or no longer accessible prior to writing: " + contentFile, e);
        } catch (IOException e) {
            throw new ContentStorageException("Error while writing content to file: " + contentFile, e);
        }
        
        if(length < 0) {
            // The file was larger than 2GB (max integer value), we need to get the length from the file instead
            length = contentFile.length(); 
        }
        
        return new FileSystemContentObject(contentFile, id.toString(), length);
    }
    
    public ContentObject getContentObject(String id) {
        File contentFile = getFileForId(id, true);
        return new FileSystemContentObject(contentFile, id, null);
    }
    
    public ContentObject updateContentObject(String id, InputStream contentStream, Long lengthHint) {
        File contentFile = getFileForId(id, true);
        
        // Write stream to a temporary file and rename when content is read successfully to prevent
        // overriding existing file and failing or keeping existing file
        File tempContentFile = new File(contentFile.getParentFile(), id + TEMP_SUFFIX);
        File oldContentFile = new File(contentFile.getParentFile(), id + OLD_SUFFIX);
        boolean tempFileCreated = false;
        long length = -1;
        
        try {
            if (!tempContentFile.createNewFile()) {
                // File already exists, being updated by another thread
                throw new ContentStorageException("Cannot update content with id: " + id + ", being updated by another user");
            }
            
            tempFileCreated = true;
            
            // Write the actual content to the file
            FileOutputStream tempOutputStream = new FileOutputStream(tempContentFile);
            length = IOUtils.copy(contentStream, tempOutputStream);
            IOUtils.closeQuietly(tempOutputStream);
            
            // Rename the content file first
            if (contentFile.renameTo(oldContentFile)) {
                if (tempContentFile.renameTo(contentFile)) {
                    // Rename was successful
                    oldContentFile.delete();
                } else {
                    // Rename failed, restore previous content
                    oldContentFile.renameTo(contentFile);
                    throw new ContentStorageException("Error while renaming new content file, content not updated");
                }
            } else {
                throw new ContentStorageException("Error while renaming existing content file, content not updated");
            }
        } catch (IOException ioe) {
            throw new ContentStorageException("Error while updating content with id: " + id, ioe);
            
        } finally {
            if (tempFileCreated) {
                try {
                    tempContentFile.delete();
                } catch(Throwable t) {
                    // No need to throw, shouldn't cause an error if the temp file cannot be deleted
                }
            }
        }
        
        return new FileSystemContentObject(contentFile, id.toString(), length);
    }

    public void deleteContentObject(String id) {
        try {
            File contentFile = getFileForId(id, true);
            contentFile.delete();
        } catch(Exception e) {
            throw new ContentStorageException("Error while deleting content", e);
        }
    }
    
    
    /**
     * @return a file reference for the given id, checking for existence based on the given flag.
     */
    protected File getFileForId(String id, boolean shouldExist) {
        BigInteger idValue = null; 
        try {
            idValue = new BigInteger(id);
        } catch(NumberFormatException nfe) {
            throw new ContentStorageException("Illegal ID value, only positive numbers are supported: " + id, nfe);
        }
        
        File path = converter.getPathForId(idValue);
        
        File file = new File(rootFolder, path.getPath());
        
        if (shouldExist != file.exists()) {
            if (shouldExist) {
                throw new ContentNotFoundException("Content with id: " + id + " was not found (path: " + 
                        file.toString() + ")");
            } else {
                throw new ContentNotFoundException("Content with id: " + id + " already exists.");
            }
        }
        return file;
    }
    
    protected BigInteger fetchNewId() {
        if (currentLeafFolder == null) {
            // Take the next available folder
            currentLeafFolder = getFirstAvailableFolder(MAX_FOLDER_RETRIES);
        }
        
        int indexToUse;
        File leafToUse = null;
        synchronized (idLock) {
            currentIndex++;
            if(currentIndex >= converter.getBlockSize()) {
                // Fetch next folder
                currentLeafFolder = getFirstAvailableFolder(MAX_FOLDER_RETRIES);
                indexToUse = 0;
                currentIndex = 0;
            } else {
                indexToUse = currentIndex;
            }
            
            leafToUse = currentLeafFolder;
        }
        
        File contentFile = new File(leafToUse, indexToUse + "");
        if(contentFile.exists()) {
            throw new ContentStorageException("Content already stored at that location, shouldn't have happended: " + contentFile);
        }
        
        try {
            if(!contentFile.createNewFile()) {
                throw new ContentStorageException("Unable to create content file: " + contentFile);
            }
        } catch(IOException ioe) {
            throw new ContentStorageException("Error while creating content file: " + contentFile, ioe);
        }
        
        return converter.getIdForPath(contentFile);
    }
    
    protected File getFirstAvailableFolder(int maxRetries) {
        
        if(maxRetries == 0) {
            logger.error("Giving up looking for next available folder, no more retries left");
            throw new ContentStorageException("Took too many attempts to get an available free folder");
        }
        
        File currentMaxFolder = rootFolder;
        File child = null;
        int[] indexes = new int[converter.getIterationDepth() - 1];
        boolean created = false;
        for(int i=0; i<converter.getIterationDepth() -1; i++) {
           created = false; 
           child = getMaxChild(currentMaxFolder);
           if(child == null) {
               // No folder yet, use 0
               child = new File(currentMaxFolder, "0");
               child.mkdir();
               created = true;
           }
           
           indexes[i] = Integer.parseInt(child.getName());
           currentMaxFolder = child;
        }
        
        if(created) {
            // No need to go to the next folder, we created the first folder in the tree ever
            return currentMaxFolder;
        } else {
            int lastIndex = indexes[converter.getIterationDepth() - 2];
            if(lastIndex >= converter.getBlockSize() - 1) {
                
                logger.debug("Block size reached, moving up one level: " + currentMaxFolder.getAbsolutePath());
                
                // Need to flip to a higher folder
                boolean needsMove = false;
                for(int i=converter.getIterationDepth() - 2; i>=0; i--) {
                    int value = indexes[i] + 1;
                    if(value <= converter.getBlockSize() - 1) {
                        // Stop looping, right level found
                        indexes[i] = value;
                        needsMove = false;
                        break;
                    } else {
                        value = 0;
                        indexes[i] = value;
                        needsMove = true;
                    }
                }
                
                if(needsMove) {
                    logger.error("Maximum number of content items reached, cannot store any more content: " + currentMaxFolder.getAbsolutePath());
                    throw new ContentStorageException("Maximum number of content items reached, cannot store any more content");
                }
                
                StringBuffer buffer = new StringBuffer();
                for(int i=0; i<indexes.length; i++) {
                    buffer.append((indexes[i]) + "").append(File.separatorChar);
                }
                
                File newFolder =  new File(rootFolder, buffer.toString());
                if(newFolder.mkdirs()) {
                    return newFolder;
                }
                
                // File already existed, repeat the process again to find the next available folder
                logger.debug("Next folder already created, retrying...");
                return getFirstAvailableFolder(--maxRetries);
            } else {
                File newFolder = new File(currentMaxFolder.getParentFile(), (lastIndex + 1) + "");
                if(newFolder.mkdir()) {
                    return newFolder;
                }
                
                // File already existed, repeat the process again to find the next available folder
                logger.debug("Next folder already created, retrying...");
                return getFirstAvailableFolder(--maxRetries);
            }
        }
    }
    
    protected File getMaxChild(File file) {
        String[] list = file.list();
        int max = -1;
        String maxChild = null;
        int current = 0;
        for(String s : list) {
            try {
                current = Integer.parseInt(s);
                
                if(current > max) {
                    maxChild = s;
                    max = current;
                }
            } catch(NumberFormatException nfe) {
                // Ignore bad named folders
                logger.warn("Content store contains bad folder: " + s);
            }
        }
        
        if(maxChild != null) {
            return new File(file, maxChild);
        }
        return null;
    }
}
