/**
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.rest.editor;

import java.nio.charset.Charset;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.model.common.ResultListDataRepresentation;
import com.activiti.service.editor.AlfrescoDecisionTableService;

/**
 * @author Tijs Rademakers
 */
@RestController
@RequestMapping("/rest/decision-table-models")
public class DecisionTablesResource {
	
    @Autowired
    protected AlfrescoDecisionTableService decisionTableService;
	
	@RequestMapping(method = RequestMethod.GET, produces = "application/json")
	public ResultListDataRepresentation getDecisionTables(@RequestParam(required=true) Long referenceId, HttpServletRequest request) {
	    // need to parse the filterText parameter ourselves, due to encoding issues with the default parsing.
        String filter = null;
        List<NameValuePair> params = URLEncodedUtils.parse(request.getQueryString(), Charset.forName("UTF-8"));
        if (params != null) {
            for (NameValuePair nameValuePair : params) {
                if ("filter".equalsIgnoreCase(nameValuePair.getName())) {
                    filter = nameValuePair.getValue();
                }
            }
        }
	    return decisionTableService.getDecisionTables(referenceId, filter);
	}
}
