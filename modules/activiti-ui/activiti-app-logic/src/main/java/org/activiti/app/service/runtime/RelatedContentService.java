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

import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.activiti.app.domain.runtime.RelatedContent;
import org.activiti.app.repository.runtime.RelatedContentRepository;
import org.activiti.content.storage.api.ContentObject;
import org.activiti.content.storage.api.ContentStorage;
import org.activiti.engine.identity.User;
import org.activiti.engine.runtime.Clock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * @author Frederik Heremans
 */
@Service
public class RelatedContentService {
    
    private static final int RELATED_CONTENT_INTERNAL_BATCH_SIZE = 256;
    
    @Autowired
    protected RelatedContentRepository contentRepository;

    @Autowired
    protected ContentStorage contentStorage;

    @Autowired
    protected Clock clock;

    public Page<RelatedContent> getRelatedContent(String source, String sourceId, int pageSize, int page) {
        PageRequest paging = new PageRequest(page, pageSize);
        return contentRepository.findAllRelatedBySourceAndSourceId(source, sourceId, paging);
    }

    public Page<RelatedContent> getRelatedContentForTask(String taskId, int pageSize, int page) {
        PageRequest paging = new PageRequest(page, pageSize);
        return contentRepository.findAllRelatedByTaskId(taskId, paging);
    }

    public Page<RelatedContent> getRelatedContentForProcessInstance(String processInstanceId, int pageSize, int page) {
        PageRequest paging = new PageRequest(page, pageSize);
        return contentRepository.findAllRelatedByProcessInstanceId(processInstanceId, paging);
    }
    
    public Page<RelatedContent> getFieldContentForProcessInstance(String processInstanceId, String field, int pageSize, int page) {
        PageRequest paging = new PageRequest(page, pageSize);
        return contentRepository.findAllByProcessInstanceIdAndField(processInstanceId, field, paging);
    }
    
    public Page<RelatedContent> getFieldContentForTask(String taskId, int pageSize, int page) {
        PageRequest paging = new PageRequest(page, pageSize);
        return contentRepository.findAllFieldBasedContentByTaskId(taskId, paging);
    }
    
    public Page<RelatedContent> getAllFieldContentForProcessInstance(String processInstanceId, int pageSize, int page) {
        PageRequest paging = new PageRequest(page, pageSize);
        return contentRepository.findAllFieldBasedContentByProcessInstanceId(processInstanceId, paging);
    }
    
    public Page<RelatedContent> getAllFieldContentForTask(String taskId, String field, int pageSize, int page) {
        PageRequest paging = new PageRequest(page, pageSize);
        return contentRepository.findAllByTaskIdAndField(taskId, field, paging);
    }

    @Transactional
    public RelatedContent createRelatedContent(User user, String name, String source, String sourceId, String taskId,
            String processId, String field, String mimeType, InputStream data, Long lengthHint) {
        
        return createRelatedContent(user, name, source, sourceId, taskId, processId, 
                mimeType, data, lengthHint, false, false, field);
    }
    
    @Transactional
    public RelatedContent createRelatedContent(User user, String name, String source, String sourceId, String taskId,
            String processId, String mimeType, InputStream data, Long lengthHint, boolean relatedContent, boolean link) {
    
        return createRelatedContent(user, name, source, sourceId, taskId, processId, mimeType, data, lengthHint, relatedContent, link, null);
    }
    
    protected RelatedContent createRelatedContent(User user, String name, String source, String sourceId, String taskId,
            String processId, String mimeType, InputStream data, Long lengthHint, boolean relatedContent, boolean link, String field) {
        
        Date timestamp = clock.getCurrentTime();
    	final RelatedContent newContent = new RelatedContent();
        newContent.setName(name);
        newContent.setSource(source);
        newContent.setSourceId(sourceId);
        newContent.setTaskId(taskId);
        newContent.setProcessInstanceId(processId);
        newContent.setCreatedBy(user.getId());
        newContent.setCreated(timestamp);
        newContent.setLastModifiedBy(user.getId());
        newContent.setLastModified(timestamp);
        newContent.setMimeType(mimeType);
        newContent.setRelatedContent(relatedContent);
        newContent.setLink(link);
        newContent.setField(field);
        
        if (data != null) {

            // Stream given, write to store and save a reference to the content object
            ContentObject createContentObject = contentStorage.createContentObject(data, lengthHint);
            newContent.setContentStoreId(createContentObject.getId());
            newContent.setContentAvailable(true);

            // After storing the stream, store the length to be accessible without having to consult the
            // underlying content storage to get file size
            newContent.setContentSize(createContentObject.getContentLength());
            
        } else {
            
            if (link) {
                // Mark content as available, since it will never be fetched and copied
                newContent.setContentAvailable(true);
            } else {
                // Content not (yet) available
                newContent.setContentAvailable(false);
            }
        }
        
        contentRepository.save(newContent);
        
        return newContent;
    }

    public RelatedContent getRelatedContent(Long id, boolean includeOwner) {
        RelatedContent content = contentRepository.findOne(id);
        
        if (content != null && includeOwner) {
            // Touch related entities
            content.getCheckoutOwner();
            content.getLockOwner();
        }

        return content;
    }
    
    @Transactional
    public void deleteRelatedContent(RelatedContent content) {
        if (content.getContentStoreId() != null) {
            final String storeId = content.getContentStoreId();
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    contentStorage.deleteContentObject(storeId);
                }
            });
        }
        
        contentRepository.delete(content);
    }

    @Transactional
    public boolean lockContent(RelatedContent content, int timeOut, User user) {
        content.setLockDate(clock.getCurrentTime());
        content.setLocked(true);
        content.setLockOwner(user.getId());

        // Set expiration date based on timeout
        Calendar expiration = Calendar.getInstance();
        expiration.setTime(content.getLockDate());
        expiration.add(Calendar.SECOND, timeOut);

        content.setLockExpirationDate(expiration.getTime());

        contentRepository.save(content);
        return true;
    }

    @Transactional
    public boolean checkout(RelatedContent content, User user, boolean toLocal) {
        content.setCheckoutDate(clock.getCurrentTime());
        content.setCheckedOut(true);
        content.setCheckedOutToLocal(toLocal);
        content.setCheckoutOwner(user.getId());

        contentRepository.save(content);
        return true;
    }

    @Transactional
    public boolean unlock(RelatedContent content) {
        content.setLockDate(null);
        content.setLockExpirationDate(null);
        content.setLockOwner(null);
        content.setLocked(false);

        contentRepository.save(content);
        return true;
    }

    @Transactional
    public boolean uncheckout(RelatedContent content) {
        content.setCheckoutDate(null);
        content.setCheckedOut(false);
        content.setCheckedOutToLocal(false);
        content.setCheckoutOwner(null);

        contentRepository.save(content);
        return true;
    }

    @Transactional
    public boolean checkin(RelatedContent content, String comment, boolean keepCheckedOut) {
        if (!keepCheckedOut) {
            content.setCheckoutDate(null);
            content.setCheckedOut(false);
            content.setCheckoutOwner(null);

            // TODO: store comment
            contentRepository.save(content);
            return true;
        }
        return false;
    }

    @Transactional
    public void updateRelatedContentData(Long relatedContentId, String contentStoreId, InputStream contentStream, Long lengthHint, User user) {
        Date timestamp = clock.getCurrentTime();
        
        ContentObject updatedContent = contentStorage.updateContentObject(contentStoreId, contentStream, lengthHint);
        
        RelatedContent relatedContent = contentRepository.findOne(relatedContentId);
        relatedContent.setLastModifiedBy(user.getId());
        relatedContent.setLastModified(timestamp);
        relatedContent.setContentSize(lengthHint);
        
        contentRepository.save(relatedContent);
    }

    @Transactional
    public void updateName(Long relatedContentId, String newName) {
        RelatedContent relatedContent = contentRepository.findOne(relatedContentId);
        relatedContent.setName(newName);
        contentRepository.save(relatedContent);
    }
    
    /**
     * Marks a piece of content as permanent and flags it being used as selected content in the given field,
     * for the given process instance id and (optional) task id.
     */
    @Transactional
    public void setContentField(Long relatedContentId, String field, String processInstanceId, String taskId) {
        final RelatedContent relatedContent = contentRepository.findOne(relatedContentId);
        relatedContent.setProcessInstanceId(processInstanceId);
        relatedContent.setTaskId(taskId);
        relatedContent.setRelatedContent(false);
        relatedContent.setField(field);
        contentRepository.save(relatedContent);
    }
    
    @Transactional
    public void storeRelatedContent(RelatedContent relatedContent) {
        contentRepository.save(relatedContent);
    }
    
    public ContentStorage getContentStorage() {
        return contentStorage;
    }

    /**
     * Deletes all content related to the given process instance. This includes all field content for a process instance, all
     * field content on tasks and all related content on tasks. The raw content data will also be removed from content storage
     * as well as all renditions and rendition data.
     */
    @Transactional
    public void deleteContentForProcessInstance(String processInstanceId) {
        int page = 0;
        Page<RelatedContent> content = contentRepository.findAllContentByProcessInstanceId(
                processInstanceId, new PageRequest(page, RELATED_CONTENT_INTERNAL_BATCH_SIZE));
        
        final Set<String> storageIds = new HashSet<String>();
        
        // Loop over all content, cascading any referencing entities
        while (content!= null) {
            for (RelatedContent relatedContent : content.getContent()) {
                
                if (relatedContent.getContentStoreId() != null) {
                    storageIds.add(relatedContent.getContentStoreId());
                }
            }
            
            // Get next page, if needed
            if (!content.isLast()) {
                page++;
                content = contentRepository.findAllContentByProcessInstanceId(
                        processInstanceId, new PageRequest(page, RELATED_CONTENT_INTERNAL_BATCH_SIZE));
            } else {
                content = null;
            }
        }
        
        // Delete raw content AFTER transaction has been committed to prevent missing content on rollback
        if(!storageIds.isEmpty()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                   for(String id : storageIds) {
                       contentStorage.deleteContentObject(id);
                   }
                }
            });
        }
        
        // Batch delete all RelatedContent entities
        contentRepository.deleteAllContentByProcessInstanceId(processInstanceId);
    }
}
