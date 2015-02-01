package org.activiti.explorer.ui.component.diagram.client.ui;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;
/**
 * Client side widget which communicates with the server. Messages from the
 * server are shown as HTML and mouse clicks are sent to the server.
 */
public class VProcessDiagram extends Widget implements Paintable
{
	public static final String ATTRIBUTE_PROC_KEY = "definitionKey";
	
	/** Set the CSS class name to allow styling. */
	public static final String CLASSNAME = "processdiagramWrapper";
	
	/** The client side widget identifier */
	protected String paintableId;

	/** Reference to the server connection object. */
	protected ApplicationConnection client;
	
	public native void drawDiagram(String processDefinitionKey)/*-{

	  $(document).ready(function(){
      var diagrams = [];
      diagrams.push(processDefinitionKey);
      
      console.log("Initialize progress bar");
      pb1 = new $.ProgressBar({
        boundingBox: '#pb1',
        label: 'Progressbar!',
        on: {
          complete: function() {
            console.log("Progress Bar COMPLETE");
            this.set('label', 'complete!');
          },
          valueChange: function(e) {
            this.set('label', e.newVal + '%');
          }
        },
        value: 0
      });
      console.log("Progress bar initiated");
      
      ProcessDiagramGenerator.options = {
        diagramBreadCrumbsId: "diagramBreadCrumbs",
        diagramHolderId: "diagramHolder",
        diagramInfoId: "diagramInfo",
      };
      ActivitiRest.options = {
        processInstanceHighLightsUrl: "http://localhost/diagram-viewer/activiti-rest/process-instance.php?id={processInstanceId}&callback=?",
        processDefinitionUrl: "http://localhost/diagram-viewer/activiti-rest/process-difinition-diagram-layout.php?id={processDefinitionId}&callback=?",
        processDefinitionByKeyUrl: "http://localhost/diagram-viewer/activiti-rest/process-definition.php?id={processDefinitionKey}&callback=?",
      };
      
      
      var processDefinitionId = processDefinitionKey;
      ProcessDiagramGenerator.drawDiagram(processDefinitionId);
      setTimeout(function(processDefinitionId) {} (processDefinitionId));
    });

	}-*/;

	/**
	 * The constructor should first call super() to initialize the component and
	 * then handle any initialization relevant to Vaadin.
	 */
	public VProcessDiagram()
	{
		super();
		DivElement element = Document.get().createDivElement();
		setElement(element);
		element.setClassName(CLASSNAME);

    DivElement barElement = Document.get().createDivElement();
    barElement.setId("pb1");
    getElement().appendChild(barElement);
    
    DivElement diagramElement = Document.get().createDivElement();
    diagramElement.setId("overlayBox");
    getElement().appendChild(diagramElement);
    
    DivElement crumbsElement = Document.get().createDivElement();
    crumbsElement.setId("diagramBreadCrumbs");
    crumbsElement.setClassName("diagramBreadCrumbs");
    crumbsElement.setAttribute("onmousedown", "onmousedown");
    crumbsElement.setAttribute("onselectstart", "onmousedown");
    diagramElement.appendChild(crumbsElement);
    
    DivElement holderElement = Document.get().createDivElement();
    holderElement.setId("diagramHolder");
    holderElement.setClassName("diagramHolder");
    diagramElement.appendChild(holderElement);
    
    DivElement infoElement = Document.get().createDivElement();
    infoElement.setId("diagramInfo");
    infoElement.setClassName("diagram-info");
    diagramElement.appendChild(infoElement);
	}

	/**
	 * Called whenever an update is received from the server
	 */
	public void updateFromUIDL(UIDL uidl, ApplicationConnection client)
	{
		if (client.updateComponent(this, uidl, true))
			return;

		this.client = client;

		// Save the client side identifier (paintable id) for the widget
		paintableId = uidl.getId();

		if (uidl.hasAttribute(ATTRIBUTE_PROC_KEY)) {
		  drawDiagram(uidl.getStringAttribute(ATTRIBUTE_PROC_KEY));
		}
	}
}
