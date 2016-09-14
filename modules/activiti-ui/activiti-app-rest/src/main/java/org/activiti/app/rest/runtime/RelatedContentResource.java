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

import javax.servlet.http.HttpServletResponse;

import org.activiti.app.model.common.ResultListDataRepresentation;
import org.activiti.app.model.runtime.RelatedContentRepresentation;
import org.activiti.app.service.exception.InternalServerErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Frederik Heremans
 */
@RestController
public class RelatedContentResource extends AbstractRelatedContentResource {

    private static final Logger logger = LoggerFactory.getLogger(AbstractRelatedContentResource.class);

    protected ObjectMapper objectMapper = new ObjectMapper();

    @RequestMapping(value = "/rest/tasks/{taskId}/content", method = RequestMethod.GET)
    public ResultListDataRepresentation getRelatedContentForTask(@PathVariable("taskId") String taskId) {
        return super.getRelatedContentForTask(taskId);
    }

    @RequestMapping(value = "/rest/process-instances/{processInstanceId}/content", method = RequestMethod.GET)
    public ResultListDataRepresentation getRelatedContentForProcessInstance(@PathVariable("processInstanceId") String processInstanceId) {
        return super.getRelatedContentForProcessInstance(processInstanceId);
    }

    @RequestMapping(value = "/rest/content/{source}/{sourceId}/process-instances", method = RequestMethod.GET)
    public ResultListDataRepresentation getRelatedProcessInstancesForContent(@PathVariable("source") String source, @PathVariable("sourceId") String sourceId) {
        return super.getRelatedProcessInstancesForContent(source, sourceId);
    }

    @RequestMapping(value = "/rest/tasks/{taskId}/raw-content", method = RequestMethod.POST)
    public RelatedContentRepresentation createRelatedContentOnTask(@PathVariable("taskId") String taskId, @RequestParam("file") MultipartFile file) {
        return super.createRelatedContentOnTask(taskId, file);
    }
            
    /*
     * specific endpoint for IE9 flash upload component
     */
    @RequestMapping(value = "/rest/tasks/{taskId}/raw-content/text", method = RequestMethod.POST)
    public String createRelatedContentOnTaskText(@PathVariable("taskId") String taskId, @RequestParam("file") MultipartFile file) {
        RelatedContentRepresentation relatedContentRepresentation = super.createRelatedContentOnTask(taskId, file);
        String relatedContentJson = null;
        try {
            relatedContentJson = objectMapper.writeValueAsString(relatedContentRepresentation);
        } catch (Exception e) {
            logger.error("Error while processing RelatedContent representation json", e);
            throw new InternalServerErrorException("Related Content on task could not be saved");
        }

        return relatedContentJson;
    }

    @RequestMapping(value = "/rest/tasks/{taskId}/content", method = RequestMethod.POST)
    public RelatedContentRepresentation createRelatedContentOnTask(@PathVariable("taskId") String taskId,
            @RequestBody RelatedContentRepresentation relatedContent) {
        return super.createRelatedContentOnTask(taskId, relatedContent);
    }

    @RequestMapping(value = "/rest/processes/{processInstanceId}/content", method = RequestMethod.POST)
    public RelatedContentRepresentation createRelatedContentOnProcessInstance(@PathVariable("processInstanceId") String processInstanceId,
            @RequestBody RelatedContentRepresentation relatedContent) {
        return super.createRelatedContentOnProcessInstance(processInstanceId, relatedContent);
    }
    
    @RequestMapping(value = "/rest/process-instances/{processInstanceId}/raw-content", method = RequestMethod.POST)
    public RelatedContentRepresentation createRelatedContentOnProcessInstance(@PathVariable("processInstanceId") String processInstanceId,
            @RequestParam("file") MultipartFile file) {
        return super.createRelatedContentOnProcessInstance(processInstanceId, file);
    }
    
    /*
     * specific endpoint for IE9 flash upload component
     */
    @RequestMapping(value = "/rest/process-instances/{processInstanceId}/raw-content/text", method = RequestMethod.POST)
    public String createRelatedContentOnProcessInstanceText(@PathVariable("processInstanceId") String processInstanceId,
            @RequestParam("file") MultipartFile file) {
        RelatedContentRepresentation relatedContentRepresentation = super.createRelatedContentOnProcessInstance(processInstanceId, file);
        
        String relatedContentJson = null;
        try {
            relatedContentJson = objectMapper.writeValueAsString(relatedContentRepresentation);
        } catch (Exception e) {
            logger.error("Error while processing RelatedContent representation json", e);
            throw new InternalServerErrorException("Related Content on process instance could not be saved");
        }

        return relatedContentJson;
    }

    @RequestMapping(value = "/rest/content/raw", method = RequestMethod.POST)
    public RelatedContentRepresentation createTemporaryRawRelatedContent(@RequestParam("file") MultipartFile file) {
        return super.createTemporaryRawRelatedContent(file);
    }
    
    /*
     * specific endpoint for IE9 flash upload component
     */
    @RequestMapping(value = "/rest/content/raw/text", method = RequestMethod.POST)
    public String createTemporaryRawRelatedContentText(@RequestParam("file") MultipartFile file) {
        RelatedContentRepresentation relatedContentRepresentation = super.createTemporaryRawRelatedContent(file);
        String relatedContentJson = null;
        try {
            relatedContentJson = objectMapper.writeValueAsString(relatedContentRepresentation);
        } catch (Exception e) {
            logger.error("Error while processing RelatedContent representation json", e);
            throw new InternalServerErrorException("Related Content could not be saved");
        }

        return relatedContentJson;
    }

    @RequestMapping(value = "/rest/content", method = RequestMethod.POST)
    public RelatedContentRepresentation createTemporaryRelatedContent(@RequestBody RelatedContentRepresentation relatedContent) {
        return addRelatedContent(relatedContent, null, null, false);
    }

    @RequestMapping(value = "/rest/content/{contentId}", method = RequestMethod.DELETE)
    public void deleteContent(@PathVariable("contentId") Long contentId, HttpServletResponse response) {
        super.deleteContent(contentId, response);
    }

    @RequestMapping(value = "/rest/content/{contentId}", method = RequestMethod.GET)
    public RelatedContentRepresentation getContent(@PathVariable("contentId") Long contentId) {
        
        return super.getContent(contentId);
    }

    @RequestMapping(value = "/rest/content/{contentId}/raw", method = RequestMethod.GET)
    public void getRawContent(@PathVariable("contentId") Long contentId, HttpServletResponse response) {
        super.getRawContent(contentId, response);
    }

}
