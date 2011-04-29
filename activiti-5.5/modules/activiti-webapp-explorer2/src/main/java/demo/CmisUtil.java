package demo;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.DocumentType;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.FolderType;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;


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

/**
 * Handy utility methods for working with CMIS
 * 
 * Do NOT delete lightly ... I spend a huge amount of time on this code!
 * 
 * @author Joram Barrez
 */
public class CmisUtil {

  /**
   * Creates a CMIS session with the given repository using the given credentials.
   */
  public static Session createCmisSession(String user, String password, String url) {
    SessionFactory sessionFactory = SessionFactoryImpl.newInstance();
    Map<String, String> parameter = new HashMap<String, String>();
    parameter.put(SessionParameter.USER, user);
    parameter.put(SessionParameter.PASSWORD, password);
    parameter.put(SessionParameter.ATOMPUB_URL, url); 
    parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());

    Session session = null;
    try {
      Repository soleRepository = sessionFactory.getRepositories(parameter).get(0);
      session = soleRepository.createSession();
    }
    catch(CmisBaseException e) {
      e.printStackTrace();
    }
    return session;
  }
  
  /**
   * Retrieves the folder with the given name.
   * Returns null if the folder does not exist.
   */
  public static Folder getFolder(Session session, String folderName) {
    ObjectType type = session.getTypeDefinition("cmis:folder");
    PropertyDefinition<?> objectIdPropDef = type.getPropertyDefinitions().get(PropertyIds.OBJECT_ID);
    String objectIdQueryName = objectIdPropDef.getQueryName();
    
    ItemIterable<QueryResult> results = session.query("SELECT * FROM cmis:folder WHERE cmis:name='" + folderName + "'", false);
    for (QueryResult qResult : results) {
       String objectId = qResult.getPropertyValueByQueryName(objectIdQueryName);
       return (Folder) session.getObject(session.createObjectId(objectId));
    }
    return null;
  }
  
  /**
   * Creates a folder under the given parent folder.
   */
  public static Folder createFolder(Session session, Folder parentFolder, String folderName) {
    Map<String, Object> folderProps = new HashMap<String, Object>();
    folderProps.put(PropertyIds.NAME, folderName);
    folderProps.put(PropertyIds.OBJECT_TYPE_ID, FolderType.FOLDER_BASETYPE_ID);

    ObjectId folderObjectId = session.createFolder(folderProps, parentFolder, null, null, null);
    return (Folder) session.getObject(folderObjectId);
  }
  
  /**
   * Creates a document with the given content in a certain folder.
   */
  public static Document createDocument(Session session, Folder folder, String fileName, byte[] content, String mimeType) throws Exception {
    Map<String, Object> docProps = new HashMap<String, Object>();
    docProps.put(PropertyIds.NAME, fileName);
    docProps.put(PropertyIds.OBJECT_TYPE_ID, DocumentType.DOCUMENT_BASETYPE_ID);

    ByteArrayInputStream in = new ByteArrayInputStream(content);
    ContentStream contentStream = session.getObjectFactory().createContentStream(fileName, content.length, mimeType, in);
    
    ObjectId documentId = session.createDocument(docProps, session.createObjectId((String) folder.getPropertyValue(PropertyIds.OBJECT_ID)), contentStream, null, null, null, null);
    Document document = (Document) session.getObject(documentId);
    return document;
  }
  
}
