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
package org.activiti.explorer.ui.process.simple.editor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.UUID;

import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.repository.Model;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ui.AbstractPage;
import org.activiti.explorer.ui.custom.DetailPanel;
import org.activiti.explorer.ui.custom.ToolBar;
import org.activiti.explorer.ui.custom.ToolbarEntry.ToolbarCommand;
import org.activiti.explorer.ui.mainlayout.ExplorerLayout;
import org.activiti.explorer.ui.process.simple.editor.table.TaskTable;
import org.activiti.image.ProcessDiagramGenerator;
import org.activiti.workflow.simple.converter.WorkflowDefinitionConversion;
import org.activiti.workflow.simple.definition.HumanStepDefinition;
import org.activiti.workflow.simple.definition.ListStepDefinition;
import org.activiti.workflow.simple.definition.ParallelStepsDefinition;
import org.activiti.workflow.simple.definition.StepDefinition;
import org.activiti.workflow.simple.definition.StepDefinitionContainer;
import org.activiti.workflow.simple.definition.WorkflowDefinition;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.terminal.StreamResource;
import com.vaadin.terminal.StreamResource.StreamSource;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.Reindeer;

/**
 * @author Joram Barrez
 */
public class SimpleTableEditor extends AbstractPage {

	private static final long serialVersionUID = -4430424035744622992L;
	
	private static final Logger logger = LoggerFactory.getLogger(SimpleTableEditor.class);
	
	private static final String KEY_EDITOR = "editor";
	private static final String KEY_PREVIEW = "preview";

	// Input when creating new process
	protected String workflowName;
	protected String description;
	
	// Input when updating existing proceess
	protected String modelId;
	protected WorkflowDefinition workflowDefinition;
	
	// ui
	protected DetailPanel mainLayout;
	protected GridLayout editorGrid;
	protected TextField nameField;
	protected TextField descriptionField;
	protected TaskTable taskTable;
	protected Panel imagePanel;
	
	/**
	 * Constructor used when creating a new process.
	 */
	public SimpleTableEditor(String workflowName, String description) {
	  this.workflowName = workflowName;
	  this.description = description;
	}
	
	/**
	 * Constructor used when updating an existing process
	 */
	public SimpleTableEditor(String modelId, WorkflowDefinition workflowDefinition) {
	  this(workflowDefinition.getName(), workflowDefinition.getDescription());
	  this.modelId = modelId;
	  this.workflowDefinition = workflowDefinition;
	}
	
	@Override
  protected void initUi() {
    super.initUi();
    setSizeFull();
    grid.setColumnExpandRatio(0, 0f); // Hide the column on the left side

	  mainLayout = new DetailPanel();
	  setDetailComponent(mainLayout);
	  
    // Editor
		editorGrid = new GridLayout(2, 7);
		editorGrid.setSizeFull();
		editorGrid.setMargin(true);
		editorGrid.setColumnExpandRatio(0, 1.0f);
		editorGrid.setColumnExpandRatio(1, 9.0f);
		editorGrid.setSpacing(true);
		mainLayout.addComponent(editorGrid);
		
		initNameField(editorGrid);
		initDescriptionField(editorGrid);
		initTaskTable(editorGrid);
		initButtons(editorGrid);
		
		toolBar.setActiveEntry(KEY_EDITOR);
	}
	
	protected ToolBar initToolbar() {
	  toolBar = new ToolBar();
	  
	  toolBar.addToolbarEntry(KEY_EDITOR, ExplorerApp.get().getI18nManager().getMessage(Messages.PROCESS_EDITOR_TITLE), new ToolbarCommand() {
      public void toolBarItemSelected() {
        if (imagePanel != null) {
          imagePanel.setVisible(false);
          editorGrid.setVisible(true);
          toolBar.setActiveEntry(KEY_EDITOR);
        }
      }
    });
	  
	  toolBar.addToolbarEntry(KEY_PREVIEW, ExplorerApp.get().getI18nManager().getMessage(Messages.PROCESS_EDITOR_BPMN_PREVIEW), new ToolbarCommand() {
      public void toolBarItemSelected() {
        editorGrid.setVisible(false);
        showDiagram();
        toolBar.setActiveEntry(KEY_PREVIEW);
      }
    });
	  
	  return toolBar;
	}

	protected void initNameField(GridLayout layout) {
		nameField = new TextField();
		nameField.setWriteThrough(true);
		nameField.setImmediate(true);

		layout.addComponent(new Label(ExplorerApp.get().getI18nManager().getMessage(Messages.PROCESS_EDITOR_NAME)));
		layout.addComponent(nameField);
		if (workflowName != null) {
		  nameField.setValue(workflowName);
		  workflowName = null;
		}
	}

	protected void initDescriptionField(GridLayout layout) {
		descriptionField = new TextField();
		descriptionField.setRows(4);
		descriptionField.setColumns(35);
		descriptionField.addStyleName(ExplorerLayout.STYLE_TEXTAREA_NO_RESIZE);
		layout.addComponent(new Label(ExplorerApp.get().getI18nManager().getMessage(Messages.PROCESS_EDITOR_DESCRIPTION)));
		layout.addComponent(descriptionField);
		
		if (description != null) {
		  descriptionField.setValue(description);
		  description = null;
		}
	}

	protected void initTaskTable(GridLayout layout) {
		taskTable = new TaskTable();
		
		// Add existing tasks in case we're editing
		if (workflowDefinition != null) {
		  loadTaskRows(workflowDefinition, taskTable);
		} else {
		  taskTable.addDefaultTaskRow();
		}

		layout.addComponent(new Label(ExplorerApp.get().getI18nManager().getMessage(Messages.PROCESS_EDITOR_TASKS)));
		layout.addComponent(taskTable);
	}
	
	protected void loadTaskRows(StepDefinitionContainer<?> container, TaskTable taskTable) {
	  for (StepDefinition stepDefinition : container.getSteps()) {
      if (stepDefinition instanceof HumanStepDefinition) {
        HumanStepDefinition humanStepDefinition = (HumanStepDefinition) stepDefinition;
        taskTable.addTaskRow(humanStepDefinition);
      } else if (stepDefinition instanceof StepDefinitionContainer<?>) {
        loadTaskRows((StepDefinitionContainer<?>) stepDefinition, taskTable);
      }
    }
	}

	protected void initButtons(GridLayout layout) {
		final Button saveButton = new Button(ExplorerApp.get().getI18nManager().getMessage(Messages.PROCESS_EDITOR_SAVE));
		saveButton.setEnabled(nameField.getValue() != null && !"".equals((String) nameField.getValue()));
		toolBar.addButton(saveButton);
		
		saveButton.addListener(new ClickListener() {
      private static final long serialVersionUID = 1L;
      public void buttonClick(ClickEvent event) {
        save();
      }
    });

		// Dependending on namefield value, save button is enabled
		nameField.addListener(new ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			public void valueChange(ValueChangeEvent event) {
				if (nameField.getValue() != null && !"".equals((String) nameField.getValue())) {
					saveButton.setEnabled(true);
				} else {
					saveButton.setEnabled(false);
				}
			}
		});

	}

	public TextField getNameTextField() {
	  return nameField;
	}
	
	protected void showDiagram() {
	  StreamResource.StreamSource streamSource = new StreamSource() {
      
      private static final long serialVersionUID = 6993112534181068935L;

      public InputStream getStream() {
        WorkflowDefinitionConversion workflowDefinitionConversion =
                ExplorerApp.get().getWorkflowDefinitionConversionFactory().createWorkflowDefinitionConversion(createWorkflow());
        final ProcessEngineImpl defaultProcessEngine = (ProcessEngineImpl) ProcessEngines.getDefaultProcessEngine();
        final ProcessEngineConfiguration processEngineConfiguration = defaultProcessEngine.getProcessEngineConfiguration();
        final ProcessDiagramGenerator diagramGenerator = processEngineConfiguration.getProcessDiagramGenerator();

        return diagramGenerator.generateDiagram(workflowDefinitionConversion.getBpmnModel(), "png", processEngineConfiguration.getActivityFontName(),
            processEngineConfiguration.getLabelFontName(), processEngineConfiguration.getAnnotationFontName(), processEngineConfiguration.getClassLoader());
      }
    };
    
    // resource must have unique id (or cache-crap can happen)!
    StreamResource imageresource = new StreamResource(streamSource,UUID.randomUUID() + ".png", ExplorerApp.get());
    Embedded diagram = new Embedded("", imageresource);
    diagram.setType(Embedded.TYPE_IMAGE);
    diagram.setSizeUndefined();
    
    imagePanel = new Panel(); // using panel for scrollbars
    imagePanel.setScrollable(true);
    imagePanel.addStyleName(Reindeer.PANEL_LIGHT);
    imagePanel.setWidth(100, UNITS_PERCENTAGE);
    imagePanel.setHeight("100%");
    mainLayout.addComponent(imagePanel);
    
    HorizontalLayout panelLayout = new HorizontalLayout();
    panelLayout.setSizeUndefined();
    imagePanel.setContent(panelLayout);
    imagePanel.addComponent(diagram);
	}
	
	protected void save() {
	  WorkflowDefinition workflowDefinition = createWorkflow();

    final ProcessEngineImpl defaultProcessEngine = (ProcessEngineImpl) ProcessEngines.getDefaultProcessEngine();
    RepositoryService repositoryService = defaultProcessEngine.getRepositoryService();
    ProcessEngineConfiguration processEngineConfiguration = defaultProcessEngine.getProcessEngineConfiguration();
    ProcessDiagramGenerator diagramGenerator = processEngineConfiguration.getProcessDiagramGenerator();
	  
	  Model model = null;
	  if (modelId == null) { // new process
	    model = repositoryService.newModel();
	  } else { // update existing process
	    model = repositoryService.getModel(modelId);
	  }

	  model.setName(workflowDefinition.getName());
    model.setCategory(SimpleTableEditorConstants.TABLE_EDITOR_CATEGORY);
    repositoryService.saveModel(model);

    // Store model entity
    WorkflowDefinitionConversion conversion = 
            ExplorerApp.get().getWorkflowDefinitionConversionFactory().createWorkflowDefinitionConversion(workflowDefinition);
    conversion.convert();
    
    try {
    	// Write JSON to byte-array and set as editor-source
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	ExplorerApp.get().getSimpleWorkflowJsonConverter().writeWorkflowDefinition(workflowDefinition, new OutputStreamWriter(baos));
      repositoryService.addModelEditorSource(model.getId(), baos.toByteArray());
      
      // Store process image
      // TODO: we should really allow the service to take an inputstream as input. Now we load it into memory ...
      repositoryService.addModelEditorSourceExtra(model.getId(), IOUtils.toByteArray(
          diagramGenerator.generateDiagram(conversion.getBpmnModel(), "png", processEngineConfiguration.getActivityFontName(),
              processEngineConfiguration.getLabelFontName(), processEngineConfiguration.getAnnotationFontName(), processEngineConfiguration.getClassLoader())));
    } catch (IOException e) {
      logger.warn("Could not generate process image. Image is not stored and will not be shown.", e);
    }
    
    ExplorerApp.get().getViewManager().showEditorProcessDefinitionPage(model.getId());
	}
	
	 protected WorkflowDefinition createWorkflow() {
	    WorkflowDefinition workflow = new WorkflowDefinition();
	    workflow.setName((String) nameField.getValue());
	    
	    String description = (String) descriptionField.getValue();
	    if (description != null && description.length() > 0) {
	      workflow.setDescription(description);
	    }
	    
	    List<HumanStepDefinition> steps = taskTable.getSteps();
	    for (int i=0; i<steps.size(); i++) {
	      HumanStepDefinition currentStep = steps.get(i);
	      
	      // Check if we have a parallel block
	      int nextIndex = i+1;
	      ParallelStepsDefinition parallelStepsDefinition = null;
	      while (nextIndex < steps.size() && steps.get(nextIndex).isStartsWithPrevious()) {
	        if (parallelStepsDefinition == null) {
	          parallelStepsDefinition = new ParallelStepsDefinition();
	          ListStepDefinition<ParallelStepsDefinition> listStepDef = new ListStepDefinition<ParallelStepsDefinition>();
	          listStepDef.addStep(currentStep);
	          parallelStepsDefinition.addStepList(listStepDef);
	        }
	        
	        ListStepDefinition<ParallelStepsDefinition> listStepDef = new ListStepDefinition<ParallelStepsDefinition>();
          listStepDef.addStep(steps.get(nextIndex));
          parallelStepsDefinition.addStepList(listStepDef);
	        nextIndex++;
	      }
	      
	      if (parallelStepsDefinition != null) {
	        workflow.addStep(parallelStepsDefinition);
	        i = nextIndex - 1;
	      } else {
	        workflow.addStep(currentStep);
	      }
	    }
	    
	    return workflow;
	  }
	
	// Following are required by superclass, but not implemented since we do the addMainLayout ourselves

  @Override
  protected ToolBar createMenuBar() {
    return initToolbar();
  }

  @Override
  protected AbstractSelect createSelectComponent() {
    return null;
  }

  @Override
  public void refreshSelectNext() {
  }

  @Override
  public void selectElement(int index) {
  }

}
