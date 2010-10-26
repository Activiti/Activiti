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
package org.activiti.engine.impl.ant;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipInputStream;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineInfo;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.util.LogUtil;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;


/**
 * @author Tom Baeyens
 */
public class DeployBarTask extends Task {
  
  String processEngineName = ProcessEngines.NAME_DEFAULT;
  File file;
  List<FileSet> fileSets;
  
  public void execute() throws BuildException {
    List<File> files = new ArrayList<File>();
    if (file!=null) {
      files.add(file);
    }
    if (fileSets!=null) {
      for (FileSet fileSet: fileSets) {
        DirectoryScanner directoryScanner = fileSet.getDirectoryScanner(getProject());
        File baseDir = directoryScanner.getBasedir();
        String[] includedFiles = directoryScanner.getIncludedFiles();
        String[] excludedFiles = directoryScanner.getExcludedFiles();
        List<String> excludedFilesList = Arrays.asList(excludedFiles);
        for (String includedFile: includedFiles) {
          if (!excludedFilesList.contains(includedFile)) {
            files.add(new File(baseDir, includedFile));
          }
        }
      }
    }
    
    Thread currentThread = Thread.currentThread();
    ClassLoader originalClassLoader = currentThread.getContextClassLoader(); 
    currentThread.setContextClassLoader(DeployBarTask.class.getClassLoader());
    
    LogUtil.readJavaUtilLoggingConfigFromClasspath();
    
    try {
      log("Initializing process engine " + processEngineName);
      ProcessEngines.init();
      ProcessEngine processEngine = ProcessEngines.getProcessEngine(processEngineName);
      if (processEngine == null) {
        List<ProcessEngineInfo> processEngineInfos = ProcessEngines.getProcessEngineInfos();
        if( processEngineInfos != null && processEngineInfos.size() > 0 )
        {
        	ProcessEngineInfo engineInfo = processEngineInfos.get(0);
        
            if(engineInfo.getException().indexOf("driver on UnpooledDataSource") != -1)
            {
                throw new ActivitiException("Exception while initializing process engine! Database or database driver might not have been configured correctly." +
                		"Please consult the user guide for supported database environments or build.properties. Exception: " + engineInfo.getException());          	  
            }
            else
            	throw new ActivitiException("Process engine '" + processEngineName + "' can not be initialized! " + engineInfo.getException());
        }
        else
        	throw new ActivitiException("Could not find a process engine with name '" + processEngineName + "'");
      }
      RepositoryService repositoryService = processEngine.getRepositoryService();

      log("Starting to deploy " + files.size() + " files");
      for (File file: files) {
        String path = file.getAbsolutePath();
        log("Handling file " + path);
        try {
          FileInputStream inputStream = new FileInputStream(file);
          try {
            log("deploying bar "+path);
            repositoryService
                .createDeployment()
                .name(file.getName())
                .addZipInputStream(new ZipInputStream(inputStream))
                .deploy();
          } finally {
            inputStream.close();
          }
        } catch (Exception e) {
          throw new BuildException("couldn't deploy bar "+path+": "+e.getMessage(), e);
        }
      }

    } finally {
      currentThread.setContextClassLoader(originalClassLoader);
    }
  }

  public String getProcessEngineName() {
    return processEngineName;
  }
  public void setProcessEngineName(String processEngineName) {
    this.processEngineName = processEngineName;
  }
  public File getFile() {
    return file;
  }
  public void setFile(File file) {
    this.file = file;
  }
  public List<FileSet> getFileSets() {
    return fileSets;
  }
  public void setFileSets(List<FileSet> fileSets) {
    this.fileSets = fileSets;
  }
}
