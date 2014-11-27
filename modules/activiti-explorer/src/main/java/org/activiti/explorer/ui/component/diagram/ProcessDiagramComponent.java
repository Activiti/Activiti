package org.activiti.explorer.ui.component.diagram;

import org.activiti.explorer.ui.component.diagram.client.ui.VProcessDiagram;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;

/**
 * Server side component for the VUsemapImage widget.
 */
@SuppressWarnings("serial")
@com.vaadin.ui.ClientWidget(VProcessDiagram.class)
public class ProcessDiagramComponent extends AbstractComponent {
  
	private String processDefinitionKey;

	public void setProcessDefinitionKey(String processDefinitionKey) {
	  this.processDefinitionKey = processDefinitionKey;
		requestRepaint();
	}

	@Override
	public void paintContent(PaintTarget target) throws PaintException
	{
		super.paintContent(target);
		target.addAttribute(VProcessDiagram.ATTRIBUTE_PROC_KEY, processDefinitionKey);
	}
}
