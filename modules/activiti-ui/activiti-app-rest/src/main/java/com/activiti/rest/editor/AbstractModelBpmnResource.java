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
package com.activiti.rest.editor;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.activiti.bpmn.model.BpmnModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.activiti.domain.editor.AbstractModel;
import com.activiti.domain.editor.Model;
import com.activiti.domain.editor.ModelHistory;
import com.activiti.security.SecurityUtils;
import com.activiti.service.editor.ModelInternalService;
import com.activiti.service.exception.BadRequestException;
import com.activiti.service.exception.BaseModelerRestException;
import com.activiti.service.exception.InternalServerErrorException;

/**
 * @author jbarrez
 */
public class AbstractModelBpmnResource extends BaseModelResource {

    private final Logger log = LoggerFactory.getLogger(AbstractModelBpmnResource.class);

	@Inject
	protected ModelInternalService modelService;

    public void getProcessModelBpmn20Xml(HttpServletResponse response, Long processModelId) throws IOException {

		if (processModelId == null) {
			throw new BadRequestException("No process model id provided");
		}

        Model model = getModel(processModelId, true, false);
        generateBpmn20Xml(response, model);
    }

    public void getHistoricProcessModelBpmn20Xml(HttpServletResponse response, Long processModelId, Long processModelHistoryId) throws IOException {

		if (processModelId == null) {
			throw new BadRequestException("No process model id provided");
		}

        ModelHistory historicModel = getModelHistory(processModelId, processModelHistoryId, true, false);
        generateBpmn20Xml(response, historicModel);
    }

	protected void generateBpmn20Xml(HttpServletResponse response, AbstractModel model) {
	    String name = model.getName().replaceAll(" ", "_");
		response.setHeader("Content-Disposition", "attachment; filename=" + name + ".bpmn20.xml");
        if (model.getModelEditorJson() != null) {
    	    try {
    	    	ServletOutputStream servletOutputStream = response.getOutputStream();
    	    	response.setContentType("application/xml");

                BpmnModel bpmnModel = modelService.getBpmnModel(model, SecurityUtils.getCurrentUserObject(), true);
                byte[] xmlBytes = modelService.getBpmnXML(bpmnModel);
                BufferedInputStream in = new BufferedInputStream(new ByteArrayInputStream(xmlBytes));

        		byte[] buffer = new byte[8096];
        		while (true) {
        			int count = in.read(buffer);
        			if (count == -1)
        				break;
        			servletOutputStream.write(buffer, 0, count);
        		}

        		// Flush and close stream
        		servletOutputStream.flush();
        		servletOutputStream.close();

    	    } catch (BaseModelerRestException e) {
    	        throw e;

            } catch (Exception e) {
            	log.error("Could not generate BPMN 2.0 XML", e);
            	throw new InternalServerErrorException("Could not generate BPMN 2.0 xml");
            }
    	}
    }
}
