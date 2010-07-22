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
package org.activiti.impl.cycle.connect.signavio.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.activiti.impl.cycle.connect.api.FileInfo;
import org.activiti.impl.cycle.connect.api.FolderInfo;
import org.activiti.impl.cycle.connect.api.ItemInfo;
import org.activiti.impl.cycle.connect.api.RepositoryConnector;
import org.activiti.impl.cycle.connect.api.actions.FileAction;
import org.activiti.impl.cycle.connect.api.actions.FileActionGuiRepresentation;
import org.activiti.impl.cycle.connect.signavio.IoUtils;
import org.activiti.impl.cycle.connect.signavio.SignavioConnector;

/**
 * @author christian.lipphardt@camunda.com
 */
public class CreateJbpm4AntProject extends FileAction {

  // TODO: read it from a property
  // private static final String PATH_TO_JBPM_PROJECT_ZIP =
  // Configuration.getInstance().getProperty("path_to_jbpm_project_zip");
  private static final String PATH_TO_JBPM_PROJECT_ZIP = "path_to_jbpm_project_zip";

  private static final List<String> replaceList = new ArrayList<String>();
  private static final String REPLACE_STRING = "camunda-fox-template-jbpm4-ant";

  static {
    replaceList.add(".project");
    replaceList.add("build.xml");
  }

  @Override
  public void execute() {
  }

  @Override
  public void execute(ItemInfo itemInfo) {
    log.info("Execute fileAction: " + this.getClass().getName());
    createJbpmProject(itemInfo, null);
  }

  @Override
  public FileActionGuiRepresentation getGuiRepresentation() {
    return FileActionGuiRepresentation.MODAL_PANEL;
  }

  @Override
  public String getGuiRepresentationAsString() {
    return FileActionGuiRepresentation.MODAL_PANEL.toString();
  }

  @Override
  public String getGuiRepresentationUrl() {
    return "modalContent/createJbpmProject.xhtml";
  }

  @Override
  public String getName() {
    return "jBPM Projekt erstellen";
  }

  public void createJbpmProject(ItemInfo itemInfo, Map<String, Object> param) {
    if (itemInfo == null || !(itemInfo instanceof FolderInfo)) {
      throw new IllegalArgumentException("No valid target folder chosen.");
    }

    String replacement = (String) param.get("input");

    // get target folder
    FolderInfo targetFolder = (FolderInfo) itemInfo;
    RepositoryConnector svnconnector = targetFolder.getConnector();
    log.info("TargetFolder-Name: " + targetFolder.getName() + " - Path: " + targetFolder.getPath());

    FileInfo srcFile = getFile();
    log.info("srcFile - Name:" + srcFile.getName() + " - Path:" + srcFile.getPath());

    File file = new File(PATH_TO_JBPM_PROJECT_ZIP);
    FileInputStream fin = null;
    ZipInputStream zin = null;

    // TODO: Think about FileInfo-API to allow for complete path instead of
    // folder object which must be access recursively
    // TODO: Fix correct folder and name discovery

    try {
      fin = new FileInputStream(file);
      zin = new ZipInputStream(fin);

      ZipEntry zipEntry = null;
      FolderInfo prevSubFolder = null;
      String prevFolderPath = null;

      while ((zipEntry = zin.getNextEntry()) != null) {
        String entryName = zipEntry.getName();

        String zipEntryPath = "";
        // String zipEntryLastFolderName = entryName.substring(0,
        // entryName.lastIndexOf("/"));
        String zipEntryFileName = entryName;

        if (entryName.lastIndexOf("/") > -1) {
          zipEntryPath = entryName.substring(0, entryName.lastIndexOf("/"));
          // String zipEntryLastFolderName = entryName.substring(0,
          // entryName.lastIndexOf("/"));
          zipEntryFileName = entryName.substring(entryName.lastIndexOf("/") + 1);
        }

        // log.info("ZipEntry: " + entryName);

        // split to check for subfolders
        // String[] splitPath = entryName.split("/");
        // for (int i = 0; i < splitPath.length; i++) {
        // log.info("splitPath[" + i + "] = " + splitPath[i]);
        // }

        if (zipEntry.isDirectory()) {
          // dir found
          // log.info("Folder found: " + entryName);

          FolderInfo subFolder = new FolderInfo(svnconnector);
          subFolder.setName(zipEntryPath);
          subFolder.setPath(targetFolder.getPath() + "/" + zipEntryPath);
          targetFolder.createFolder(subFolder);

          prevSubFolder = subFolder;
          prevFolderPath = zipEntryPath;

          log.info("Created folder: " + subFolder.getName() + " on " + targetFolder.getName() + " with path " + subFolder.getPath());
        } else {
          FolderInfo fileParentFolder = null;
          FileInfo destFile = new FileInfo(svnconnector);

          if ("template.xml".equals(zipEntryFileName)) {
            // process template file found
            // TODO: UGLY AGAIN!!!! Refsctor and make more intelligent and less
            // hard coded

            SignavioConnector signavioConnector = (SignavioConnector) srcFile.getConnector();
            String jpdl4Representation = signavioConnector.getModelAsJpdl4Representation(srcFile);

            destFile.setName(srcFile.getName() + ".jpdl.xml");
            destFile.setTextContent(jpdl4Representation);

            log.info("destFile is processdefinition from Signavio with name " + destFile.getName());
          } else {
            // file found
            destFile.setName(zipEntryFileName);
            log.info("destFile from ZIP with name " + destFile.getName());
            // destFile.setPath(entryName);
            // log.info("destFile - Path: " + destFile.getPath());

            // set file binary data
            // byte[] bytes = IoUtils.readBytes(zis);
            // destFile.setBinaryContent(bytes);

            String textData = new String(IoUtils.readBytes(zin));

            if (replaceList.contains(destFile.getName().toLowerCase()) || replacement.length() == 0 || replacement != null) {
              // replace in string
              destFile.setTextContent(textData.replaceAll(REPLACE_STRING, replacement));
            } else {
              // set file text data
              destFile.setTextContent(textData);
            }
          }

          // create destFile in svn
          // TODO: Wohoo, that is ugly and only works because the ordering of
          // the ZIP-API
          if (prevSubFolder == null || !prevFolderPath.equals(zipEntryPath)) {
            fileParentFolder = targetFolder;
          } else {
            fileParentFolder = prevSubFolder;
          }

          log.info("Create file " + destFile.getName() + " in folder " + fileParentFolder.getName() + " (Path = " + fileParentFolder.getPath() + ")");
          svnconnector.createNewFile(fileParentFolder, destFile);
        }

        // check for subfolders to create them if needed
        // if (checkForPath.length > 1) {
        // // subFolders found, they need to be created
        // int i = 0;
        // while (i < (checkForPath.length - 1)) {
        // FolderInfo subFolder = new FolderInfo(svnconnector);
        // String subFolderName = checkForPath[i];
        // subFolder.setName(subFolderName);
        // targetFolder.createFolder(subFolder);
        // log.info("Created subFolder " + subFolder.getName());
        // i++;
        // }
        // }
        zin.closeEntry();
      }

      log.info("Succesfully created JbpmProject!");
    } catch (FileNotFoundException fnfe) {
      // TODO: Add correct exception handling!
      log.log(Level.SEVERE, "FileNotFoundException", fnfe);
    } catch (Exception e) {
      log.log(Level.SEVERE, "Error", e);
    } finally {
      try {
        if (fin != null) {
          fin.close();
        }
        if (zin != null) {
          zin.close();
        }
      } catch (Exception e) {
        // TODO: handle exception
      }
    }
  }

  @Override
  public void execute(ItemInfo itemInfo, Map<String, Object> param) {
    log.info("Execute fileAction: " + this.getClass().getName());
    createJbpmProject(itemInfo, param);
  }
}
