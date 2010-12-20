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

package org.activiti.test.cactus.ant;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

/**
 * An ant-task that lists all Tests that are found in a given file-set and
 * creates a file containing the class-names of all found tests.
 * 
 * @author Frederik Heremans
 */
public class ListTestsTask extends Task {

  private static final String PACKAGE_ROOT = "org";
  private static final String PACKAGE_SUB_ROOT = "activiti";
  
  private FileSet testfileset;
  private File targetfile;
  private List<String> excludedTests;

  public void addTestfileset(FileSet fileSet) {
   testfileset = fileSet;
  }
  
  
  @Override
  public void execute() throws BuildException {

    checkParams();
    PrintWriter writer = null;
    try {

      // Create file, if it doesn't exist already
      if (!targetfile.exists()) {
        targetfile.createNewFile();
      }
      writer = new PrintWriter(targetfile);

      // Run through all files, to see of the file is a java test class
      List<File> files = getIncludedFilesFromFileSet();
      for (File file : files) {
        // TODO: more advanced checking if a file is a test?
        if (file.getName().endsWith("Test.class") && !isTestExcluded(file.getAbsolutePath())) {
          // Convert the path to a classname
          List<String> parts = getPartsFromFile(file);
          String className = getClassNameFromFileParts(parts);
          if(className != null) {
              writer.println(className);
          } else {
            // Log that there was a file that matches test-pattern, but is not
            // in the right package
            log("Ignoring file, invalid package: " + file.getAbsolutePath());
          }
        }
      }

    } catch (IOException ioe) {
      throw new BuildException("Errow while writing to result-file", ioe);
    } finally {
      closeSilently(writer);
    }
  }
  
  public String getClassNameFromFileParts(List<String> parts) {
    // Run through the path to find package root and sub-root
    for(int i=0; i < parts.size() - 1; i++) {
      if(PACKAGE_ROOT.equals(parts.get(i)) && PACKAGE_SUB_ROOT.equals(parts.get(i+1))) {
        // It's a valid package, concatenate parts using a '.'
       return getClassName(parts, i);
      }
    }
    return null;
  }

  private String getClassName(List<String> parts, int packageRootIndex) {
   StringBuffer buffer = new StringBuffer();
   
   for(int i = packageRootIndex; i < parts.size(); i++) {
     if(i == parts.size() -1) {
       buffer.append(parts.get(i).replace(".class", ""));
     } else {
       buffer.append(parts.get(i)).append(".");
     }
   }
   
   return buffer.toString();
  }

  private List<String> getPartsFromFile(File file) {
    List<String> parts = new ArrayList<String>();
    while(file != null) {
      if(file.getName() != null && file.getName().length() > 0) {
        parts.add(file.getName());        
      }
      file = file.getParentFile();
    }
    
    Collections.reverse(parts);
    
    return parts;
  }

  private List<File> getIncludedFilesFromFileSet() {
    List<File> files = new ArrayList<File>();
    DirectoryScanner directoryScanner = testfileset.getDirectoryScanner(getProject());
    File baseDir = directoryScanner.getBasedir();
    
    String[] includedFiles = directoryScanner.getIncludedFiles();
    String[] excludedFiles = directoryScanner.getExcludedFiles();
    List<String> excludedFilesList = Arrays.asList(excludedFiles);
    
    for (String includedFile : includedFiles) {
      if (!excludedFilesList.contains(includedFile)) {
        files.add(new File(baseDir, includedFile));
      }
    }
    return files;
  }

  private void checkParams() {
    if (testfileset == null) {
      throw new IllegalArgumentException("Task parameter testFileSet is mandatory");
    }
    if (targetfile == null) {
      throw new IllegalArgumentException("Task parameter targetFile is mandatory");
    }
  }

  private boolean isTestExcluded(String absolutePath) {
    if (excludedTests != null) {
      for (String exclude : excludedTests) {
        if (absolutePath.contains(exclude)) {
          return true;
        }
      }
    }
    return false;
  }

  private void closeSilently(Writer writer) {
    if (writer != null) {
      try {
        writer.close();
      } catch (IOException ioe) {
        // Ignore
      }
    }
  }


  
  public List<String> getExcludedTests() {
    return excludedTests;
  }
  
  public void setExcludedTests(List<String> excludedTests) {
    this.excludedTests = excludedTests;
  }


  
  public File getTargetfile() {
    return targetfile;
  }


  
  public void setTargetfile(File targetfile) {
    this.targetfile = targetfile;
  }
}
