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
package com.activiti.service.runtime;

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
