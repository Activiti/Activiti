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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;


/**
 * @author Tom Baeyens
 */
public class LaunchTask extends Task {

  private static final String FILESEPARATOR = System.getProperty("file.separator");
  
  File dir;
  String script;
  String msg;
  
  public void execute() throws BuildException {
    if (dir==null) {
      throw new BuildException("dir attribute is required with the launch task");
    }    
    if (script==null) {
      throw new BuildException("script attribute is required with the launch task");
    }    
    
    String executable = getExecutable();
    String[] cmd = new String[]{executable};
    LaunchThread.launch(this,cmd,dir,msg);
  }

  public String getExecutable() {
    String os = System.getProperty("os.name").toLowerCase();
    String dirPath = dir.getAbsolutePath();
    String base = dirPath+FILESEPARATOR+script;
    if (exists(base)) {
      return base;
    }
    
    if (os.indexOf("windows")!=-1) {
      if (exists(base+".exe")) {
        return base+".exe";
      }
      if (exists(base+".bat")) {
        return base+".bat";
      }
    }
      
    if (os.indexOf("linux")!=-1 || os.indexOf("mac")!=-1) {
      if (exists(base+".sh")) {
        return base+".sh";
      }
    }
  
    throw new BuildException("couldn't find executable for script "+base);
  }

  public boolean exists(String path) {
    File file = new File(path);
    return (file.exists());
  }

  public void setDir(File dir) {
    this.dir = dir;
  }

  
  public void setScript(String script) {
    this.script = script;
  }

  
  public void setMsg(String msg) {
    this.msg = msg;
  }
  
}
