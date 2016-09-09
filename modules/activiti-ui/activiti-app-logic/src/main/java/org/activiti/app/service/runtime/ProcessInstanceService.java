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

import javax.inject.Inject;

import org.activiti.engine.HistoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProcessInstanceService {

    @Inject
    protected RelatedContentService relatedContentService;
    
    @Inject
    protected CommentService commentService;
    
    @Inject
    protected HistoryService historyService;
    
    @Transactional
    public void deleteProcessInstance(String processInstanceId) {
        // Delete all content related to the process instance
        relatedContentService.deleteContentForProcessInstance(processInstanceId);
        
        // Delete all comments on tasks and process instances
        commentService.deleteAllCommentsForProcessInstance(processInstanceId);
        
        // Finally, delete all history for this instance in the engine
        historyService.deleteHistoricProcessInstance(processInstanceId);
    }
}
