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
package org.activiti.app.rest.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.activiti.app.domain.runtime.RelatedContent;
import org.activiti.app.model.common.ResultListDataRepresentation;
import org.activiti.app.model.component.SimpleContentTypeMapper;
import org.activiti.app.model.runtime.ProcessInstanceRepresentation;
import org.activiti.app.model.runtime.RelatedContentRepresentation;
import org.activiti.app.security.SecurityUtils;
import org.activiti.app.service.api.UserCache;
import org.activiti.app.service.exception.BadRequestException;
import org.activiti.app.service.exception.InternalServerErrorException;
import org.activiti.app.service.exception.NotFoundException;
import org.activiti.app.service.exception.NotPermittedException;
import org.activiti.app.service.runtime.PermissionService;
import org.activiti.app.service.runtime.RelatedContentService;
import org.activiti.app.service.runtime.RelatedContentStreamProvider;
import org.activiti.editor.language.json.converter.util.CollectionUtils;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.task.Task;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

public abstract class AbstractRelatedContentResource {
    
    private static final int MAX_CONTENT_ITEMS = 50;

    @Autowired
    protected PermissionService permissionService;
    
    @Autowired
    protected RelatedContentService contentService;
    
    @Autowired
    protected RelatedContentStreamProvider streamProvider;
    
    @Autowired
    protected TaskService taskService;
    
    @Autowired
    protected SimpleContentTypeMapper simpleTypeMapper;

    @Autowired
    protected HistoryService historyService;

    @Autowired
    protected RepositoryService repositoryService;

    @Autowired
    protected UserCache userCache;

    @Autowired
    protected RelatedContentStreamProvider relatedContentStreamProvider;

    public ResultListDataRepresentation getRelatedContentForTask(String taskId) {
        permissionService.validateReadPermissionOnTask(SecurityUtils.getCurrentUserObject(), taskId);
        return createResultRepresentation(contentService.getRelatedContentForTask(taskId, MAX_CONTENT_ITEMS, 0));
    }

    public ResultListDataRepresentation getRelatedContentForProcessInstance(String processInstanceId) {
        // TODO: check if process exists
        if(!permissionService.hasReadPermissionOnProcessInstance(SecurityUtils.getCurrentUserObject(), processInstanceId)) {
            throw new NotPermittedException("You are not allowed to read the process with id: " + processInstanceId);
        }
        return createResultRepresentation(contentService.getRelatedContentForProcessInstance(processInstanceId, MAX_CONTENT_ITEMS, 0));
    }
    
    public RelatedContentRepresentation createRelatedContentOnTask(String taskId, MultipartFile file) {
        User user = SecurityUtils.getCurrentUserObject();
        
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if(task == null) {
            throw new NotFoundException("Task not found or already completed: " + taskId);
        }
        
        if(!permissionService.canAddRelatedContentToTask(user, taskId)) {
            throw new NotPermittedException("You are not allowed to read the task with id: " + taskId);
        }
        return uploadFile(user, file, taskId, task.getProcessInstanceId());
    }
    
    public RelatedContentRepresentation createRelatedContentOnTask(String taskId, RelatedContentRepresentation relatedContent) {
        User user = SecurityUtils.getCurrentUserObject();
        
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if(task == null) {
            throw new NotFoundException("Task not found or already completed: " + taskId);
        }
        
        if(!permissionService.canAddRelatedContentToTask(user, taskId)) {
            throw new NotPermittedException("You are not allowed to read the task with id: " + taskId);
        }
        return addRelatedContent(relatedContent, taskId, task.getProcessInstanceId(), true);
    }
    
    public RelatedContentRepresentation createRelatedContentOnProcessInstance(String processInstanceId, RelatedContentRepresentation relatedContent) {
        User user = SecurityUtils.getCurrentUserObject();
        
        if(!permissionService.canAddRelatedContentToProcessInstance(user, processInstanceId)) {
            throw new NotPermittedException("You are not allowed to read the process with id: " + processInstanceId);
        }
        
        return addRelatedContent(relatedContent, null, processInstanceId, true);
    }
    
    public RelatedContentRepresentation createRelatedContentOnProcessInstance(String processInstanceId, MultipartFile file) {
        User user = SecurityUtils.getCurrentUserObject();
        
        if(!permissionService.canAddRelatedContentToProcessInstance(user, processInstanceId)) {
            throw new NotPermittedException("You are not allowed to read the process with id: " + processInstanceId);
        }
        return uploadFile(user, file, null, processInstanceId);
    }
    
    
    public RelatedContentRepresentation createTemporaryRawRelatedContent(MultipartFile file) {
        User user = SecurityUtils.getCurrentUserObject();
        return uploadFile(user, file, null, null);
    }
    
    
    public RelatedContentRepresentation createTemporaryRelatedContent(RelatedContentRepresentation relatedContent) {
        return addRelatedContent(relatedContent, null, null, false);
    }
    
    public void deleteContent(Long contentId, HttpServletResponse response) {
        RelatedContent content = contentService.getRelatedContent(contentId, false);
        
        if (content == null) {
            throw new NotFoundException("No content found with id: " + contentId);
        }
        
        if (!permissionService.hasWritePermissionOnRelatedContent(SecurityUtils.getCurrentUserObject(), content)) {
            throw new NotPermittedException("You are not allowed to delete the content with id: " + contentId);
        }
        
        if (content.getField() != null) {
            // Not allowed to delete content that has been added as part of a form
            throw new NotPermittedException("You are not allowed to delete the content with id: " + contentId);
        }
        
        contentService.deleteRelatedContent(content);
    }
    
    public RelatedContentRepresentation getContent(Long contentId) {
        RelatedContent content = contentService.getRelatedContent(contentId, false);
        
        if (content == null) {
            throw new NotFoundException("No content found with id: " + contentId);
        }
        
        if (!permissionService.canDownloadContent(SecurityUtils.getCurrentUserObject(), content)) {
            throw new NotPermittedException("You are not allowed to view the content with id: " + contentId);
        }
        
        return createRelatedContentResponse(content);
    }
    
    public void getRawContent(Long contentId, HttpServletResponse response) {
        RelatedContent content = contentService.getRelatedContent(contentId, false);
        
        if (content == null) {
            throw new NotFoundException("No content found with id: " + contentId);
        }
        if (!content.isContentAvailable() || (content.getContentStoreId() == null && !content.isLink())) {
            throw new NotFoundException("Raw content not yet available for id: " + contentId);
        }
        
        if (!permissionService.canDownloadContent(SecurityUtils.getCurrentUserObject(), content)) {
            throw new NotPermittedException("You are not allowed to read the content with id: " + contentId);
        }

        // Set correct mine-type
        if (content.getMimeType() != null) {
            response.setContentType(content.getMimeType());
        }
        
        // Write content response
        InputStream inputstream = null;
        try {
            inputstream = streamProvider.getContentStream(content);
            IOUtils.copy(inputstream, response.getOutputStream());
        } catch (IOException e) {
            throw new InternalServerErrorException("Error while writing raw content data for content: " + contentId, e);
        } finally {
            // Be sure to close the content object stream to free any resources that may be related to it
            if (inputstream != null) {
                IOUtils.closeQuietly(inputstream);
            }
        }
    }


    public ResultListDataRepresentation getRelatedProcessInstancesForContent(String source, String sourceId) {
        Page<RelatedContent> relatedContents = contentService.getRelatedContent(source, sourceId, MAX_CONTENT_ITEMS, 0);
        Set<String> processInstanceIds = new HashSet<String>(relatedContents.getSize());
        for(RelatedContent relatedContent : relatedContents)
        {
            processInstanceIds.add(relatedContent.getProcessInstanceId());
        }
        List<HistoricProcessInstance> processInstances;
        if (processInstanceIds.isEmpty()) {
            processInstances = new LinkedList<HistoricProcessInstance>();
        }
        else {
            // todo consider using runtimeService and ProcessInstance queries instead, but then ProcessInstance must return start user id
            HistoricProcessInstanceQuery processInstanceQuery = historyService.createHistoricProcessInstanceQuery();
            User currentUser = SecurityUtils.getCurrentUserObject();
            processInstanceQuery.involvedUser(String.valueOf(currentUser.getId()));
            processInstanceQuery.processInstanceIds(processInstanceIds);
            processInstanceQuery.orderByProcessInstanceId().desc();
            processInstances = processInstanceQuery.listPage(0, MAX_CONTENT_ITEMS);
        }
        ResultListDataRepresentation result = new ResultListDataRepresentation(convertInstanceList(processInstances));
        return result;
    }

    protected List<ProcessInstanceRepresentation> convertInstanceList(List<HistoricProcessInstance> instances) {
        List<ProcessInstanceRepresentation> result = new ArrayList<ProcessInstanceRepresentation>();
        if (CollectionUtils.isNotEmpty(instances)) {

            for (HistoricProcessInstance processInstance : instances) {
                User userRep = null;
                if(processInstance.getStartUserId() != null) {
                    UserCache.CachedUser user = userCache.getUser(processInstance.getStartUserId());
                    if(user != null && user.getUser() != null) {
                        userRep = user.getUser();
                    }
                }

                ProcessDefinitionEntity procDef = (ProcessDefinitionEntity) repositoryService.getProcessDefinition(processInstance.getProcessDefinitionId());
                ProcessInstanceRepresentation instanceRepresentation = new ProcessInstanceRepresentation(
                        processInstance, procDef, procDef.isGraphicalNotationDefined(), userRep);
                result.add(instanceRepresentation);
            }

        }
        return result;
    }

    protected RelatedContentRepresentation uploadFile(User user, MultipartFile file, String taskId, String processInstanceId) {
        if (file != null && file.getName() != null) {
            try {
                String contentType = file.getContentType();
                
                // temp additional content type check for IE9 flash uploads
                if (StringUtils.equals(file.getContentType(), "application/octet-stream")) {
                    contentType = getContentTypeForFileExtension(file);
                }
                
                RelatedContent relatedContent = contentService.createRelatedContent(user, getFileName(file), null, null, taskId, processInstanceId, 
                        contentType, file.getInputStream(), file.getSize(), true, false);
                return new RelatedContentRepresentation(relatedContent, simpleTypeMapper);
            } catch (IOException e) {
                throw new BadRequestException("Error while reading file data", e);
            }
        } else {
            throw new BadRequestException("File to upload is missing");
        }
    }
    
    protected RelatedContentRepresentation addRelatedContent(RelatedContentRepresentation relatedContent, String taskId, String processInstanceId, boolean isRelatedContent) {
        
        if (relatedContent.getSource() == null || relatedContent.getSourceId() == null || relatedContent.getName() == null) {
            throw new BadRequestException("Name, source and sourceId are required paremeters");
        }
        
        RelatedContent result = contentService.createRelatedContent(SecurityUtils.getCurrentUserObject(), 
                relatedContent.getName(), relatedContent.getSource(), relatedContent.getSourceId(), taskId, 
                processInstanceId, relatedContent.getMimeType(), null, null, isRelatedContent, relatedContent.isLink());
        
        return new RelatedContentRepresentation(result, simpleTypeMapper);
    }
    
    protected String getFileName(MultipartFile file) {
        return file.getOriginalFilename() != null ? file.getOriginalFilename() : "Nameless file";
    }
    
    protected ResultListDataRepresentation createResultRepresentation(Page<RelatedContent> results) {
        List<RelatedContentRepresentation> resultList = new ArrayList<RelatedContentRepresentation>(results.getNumberOfElements());

        for (RelatedContent content : results) {
            resultList.add(createRelatedContentResponse(content));
        }
        
        ResultListDataRepresentation result = new ResultListDataRepresentation(resultList);
        result.setTotal(results.getTotalElements());
        return result;
    }
    
    protected RelatedContentRepresentation createRelatedContentResponse(RelatedContent relatedContent) {
        RelatedContentRepresentation relatedContentResponse = new RelatedContentRepresentation(relatedContent, simpleTypeMapper);
        return relatedContentResponse;
    }
    
    protected String getContentTypeForFileExtension(MultipartFile file) {
        
    	String fileName = file.getOriginalFilename();
        String contentType = null;
        if (fileName.endsWith(".jpeg") || fileName.endsWith(".jpg")) {
            contentType = "image/jpeg";
        } else if (fileName.endsWith("gif")) {
            contentType = "image/gif";
        } else if (fileName.endsWith("png")) {
            contentType = "image/png";
        } else if (fileName.endsWith("bmp")) {
            contentType = "image/bmp";
        } else if (fileName.endsWith("tif") || fileName.endsWith(".tiff")) {
            contentType = "image/tiff";
        } else if (fileName.endsWith("png")) {
            contentType = "image/png";
        } else if (fileName.endsWith("doc")) {
            contentType = "application/msword";
        } else if (fileName.endsWith("docx")) {
            contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        } else if (fileName.endsWith("docm")) {
            contentType = "application/vnd.ms-word.document.macroenabled.12";
        } else if (fileName.endsWith("dotm")) {
            contentType = "application/vnd.ms-word.template.macroenabled.12";
        } else if (fileName.endsWith("odt")) {
            contentType = "application/vnd.oasis.opendocument.text";
        } else if (fileName.endsWith("ott")) {
            contentType = "application/vnd.oasis.opendocument.text-template";
        } else if (fileName.endsWith("rtf")) {
            contentType = "application/rtf";
        } else if (fileName.endsWith("txt")) {
            contentType = "application/text";
        } else if (fileName.endsWith("xls")) {
            contentType = "application/vnd.ms-excel";
        } else if (fileName.endsWith("xlsx")) {
            contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        } else if (fileName.endsWith("xlsb")) {
            contentType = "application/vnd.ms-excel.sheet.binary.macroenabled.12";
        } else if (fileName.endsWith("xltx")) {
            contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.template";
        } else if (fileName.endsWith("ods")) {
            contentType = "application/vnd.oasis.opendocument.spreadsheet";
        } else if (fileName.endsWith("ppt")) {
            contentType = "application/vnd.ms-powerpoint";
        } else if (fileName.endsWith("pptx")) {
            contentType = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
        } else if (fileName.endsWith("ppsm")) {
            contentType = "application/vnd.ms-powerpoint.slideshow.macroenabled.12";
        } else if (fileName.endsWith("ppsx")) {
            contentType = "application/vnd.openxmlformats-officedocument.presentationml.slideshow";
        } else if (fileName.endsWith("odp")) {
            contentType = "application/vnd.oasis.opendocument.presentation";
        } else {
        	// We've done what we could
        	return file.getContentType();
        }
        
        return contentType;
    }
}
