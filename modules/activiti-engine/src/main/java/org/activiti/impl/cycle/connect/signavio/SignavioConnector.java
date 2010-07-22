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
package org.activiti.impl.cycle.connect.signavio;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.activiti.impl.cycle.connect.api.FileInfo;
import org.activiti.impl.cycle.connect.api.FileType;
import org.activiti.impl.cycle.connect.api.FileTypeRegistry;
import org.activiti.impl.cycle.connect.api.FolderInfo;
import org.activiti.impl.cycle.connect.api.RepositoryConnector;
import org.activiti.impl.cycle.connect.api.RepositoryException;
import org.activiti.impl.cycle.connect.signavio.actions.CopyBpmn20ToSvnAction;
import org.activiti.impl.cycle.connect.signavio.actions.CopyJpdl4ToSvnAction;
import org.activiti.impl.cycle.connect.signavio.actions.CreateJbpm4AntProject;
import org.activiti.impl.cycle.connect.signavio.actions.OpenModelerAction;
import org.activiti.impl.cycle.connect.signavio.actions.ShowModelViewerAction;
import org.activiti.impl.cycle.connect.signavio.actions.ShowPngAction;
import org.activiti.impl.cycle.connect.signavio.actions.ShowjPdl4Action;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.DomRepresentation;
import org.restlet.resource.Representation;
import org.restlet.util.Series;


/**
 * TODO: refactor to differentiate between enterprise and os signavio.
 * 
 * @author christian.lipphardt@camunda.com
 */
public class SignavioConnector implements RepositoryConnector {

	public static String HOST;
	public static String SERVER_URL;
	public static String REGISTRATION_URL;
	public static String LOGIN_URL;
	public static String EDITOR_URL;
	public static String EXPLORER_URL;
	public static String MODEL_URL;
	public static String DIRECTORY_URL;
	public static String MASHUP_URL;

	// register Signavio stencilsets to identify file types
	public static final String SIGNAVIO_BPMN_2_0 = "http://b3mn.org/stencilset/bpmn2.0#";
	public static final String SIGNAVIO_BPMN_JBPM4 = "http://b3mn.org/stencilset/jbpm4#";

	static {
		// initialize associated file types
		FileTypeRegistry.registerFileType(new FileType("Signavio BPMN 2.0", SIGNAVIO_BPMN_2_0));
		FileTypeRegistry.registerFileType(new FileType("Signavio BPMN for jBPM 4", SIGNAVIO_BPMN_JBPM4));

		FileTypeRegistry.registerFileAction(SIGNAVIO_BPMN_2_0, OpenModelerAction.class);
		FileTypeRegistry.registerFileAction(SIGNAVIO_BPMN_2_0, ShowPngAction.class, true);
		FileTypeRegistry.registerFileAction(SIGNAVIO_BPMN_2_0, CopyBpmn20ToSvnAction.class);
		FileTypeRegistry.registerFileAction(SIGNAVIO_BPMN_2_0, ShowModelViewerAction.class);

		FileTypeRegistry.registerFileAction(SIGNAVIO_BPMN_JBPM4, OpenModelerAction.class);
		FileTypeRegistry.registerFileAction(SIGNAVIO_BPMN_JBPM4, ShowPngAction.class, true); // default
		FileTypeRegistry.registerFileAction(SIGNAVIO_BPMN_JBPM4, ShowjPdl4Action.class);
		FileTypeRegistry.registerFileAction(SIGNAVIO_BPMN_JBPM4, CopyJpdl4ToSvnAction.class);
		FileTypeRegistry.registerFileAction(SIGNAVIO_BPMN_JBPM4, ShowModelViewerAction.class);
		FileTypeRegistry.registerFileAction(SIGNAVIO_BPMN_JBPM4, CreateJbpm4AntProject.class);
	}

	private static Logger log = Logger.getLogger(SignavioConnector.class.getName());
	
	private static final String SERVER_SECURITY_ID = "000000"; // Captcha ID for REST access to signavio

	private String securityToken = "";
	private List<Cookie> securityCookieList = new ArrayList<Cookie>();
	
	public SignavioConnector(String url) {
		HOST = url;
		SERVER_URL = HOST + "/p";
		REGISTRATION_URL = SERVER_URL + "/register";
		LOGIN_URL = SERVER_URL + "/login";
		EDITOR_URL = SERVER_URL + "/editor";
		EXPLORER_URL = SERVER_URL + "/explorer";
		MODEL_URL = SERVER_URL + "/model";
		DIRECTORY_URL = SERVER_URL + "/directory";
		MASHUP_URL = HOST + "/mashup";
	}
	
	private Client initClient() {
		// Create and initialize HTTP client for HTTP REST API calls
		Client client = new Client(new Context(), Protocol.HTTP);
		client.getContext().getParameters().add("converter", "com.noelios.restlet.http.HttpClientConverter");

		return client;
	}

	public boolean registerUserWithSignavio(String firstname, String lastname, String email, String password) {
		Client client = initClient();

		Reference registrationRef = new Reference(REGISTRATION_URL);

		// Create the Post Parameters for registering a new user
		Form registrationForm = new Form();
		registrationForm.add("mode", "external");
		registrationForm.add("firstName", firstname);
		registrationForm.add("lastName", lastname);
		registrationForm.add("mail", email);
		registrationForm.add("password", password);
		registrationForm.add("serverSecurityId", SERVER_SECURITY_ID);
		Representation registrationRep = registrationForm.getWebRepresentation();

		Request registrationRequest = new Request(Method.POST, registrationRef, registrationRep);
		// registrationRequest.getClientInfo().getAcceptedMediaTypes().add(new
		// Preference<MediaType>(MediaType.APPLICATION_JSON));
		Response registrationResponse = client.handle(registrationRequest);

		if (log.isLoggable(Level.FINEST)) {
			SignavioLogHelper.logCookieAndBody(log, registrationResponse);
		}

		if (registrationResponse.getStatus().equals(Status.SUCCESS_CREATED)) {
			return true;
		} else {
			return false;
		}
	}

	public boolean loginIntoSignavio(String username, String password) {
		try {
			Client client = initClient();

			log.info("Logging into Signavio on url: " + LOGIN_URL);

			Reference loginRef = new Reference(LOGIN_URL);

			// Login a user
			Form loginForm = new Form();
			// loginForm.add("mode", "external");
			loginForm.add("name", username);
			loginForm.add("password", password);
			loginForm.add("tokenonly", "true");
			// loginForm.add("serverSecurityId", "000000");
			// loginForm.add("remember", "on");
			// loginForm.add("fragment", "");
			Representation loginRep = loginForm.getWebRepresentation();

			Request loginRequest = new Request(Method.POST, loginRef, loginRep);
			Response loginResponse = client.handle(loginRequest);

			if (log.isLoggable(Level.FINEST)) {
				Series<CookieSetting> cookieSentByServer = loginResponse.getCookieSettings();
				for (Iterator<Entry<String, String>> it = cookieSentByServer.getValuesMap().entrySet().iterator(); it.hasNext();) {
					Entry<String, String> cookieParam = (Entry<String, String>) it.next();
					Cookie tempCookie = new Cookie(cookieParam.getKey(), cookieParam.getValue());
					securityCookieList.add(tempCookie);
				}
			}

			log.finest("SecurityCookieList: " + securityCookieList);
			securityToken = loginResponse.getEntity().getText();
			log.finest("SecurityToken: " + securityToken);

			if (log.isLoggable(Level.FINEST)) {
				SignavioLogHelper.logCookieAndBody(log, loginResponse);
			}

			if (securityToken != null && securityToken.length() != 0) {
				return true;
			} else {
				return false;
			}
		} catch (Exception ex) {
			throw new RepositoryException("Exception during login to Signavio", ex);
		}
	}

	public JSONObject getPublicRootDirectory() throws IOException, JSONException {

		Client client = initClient();

		Reference directoryRef = new Reference(DIRECTORY_URL);

		Request directoryRequest = new Request(Method.GET, directoryRef);
		directoryRequest.getClientInfo().getAcceptedMediaTypes().add(new Preference<MediaType>(MediaType.APPLICATION_JSON));

		Form requestHeaders = new Form();
		requestHeaders.add("token", securityToken);
		directoryRequest.getAttributes().put("org.restlet.http.headers", requestHeaders);

		Response directoryResponse = client.handle(directoryRequest);
		JsonRepresentation jsonData = new JsonRepresentation(directoryResponse.getEntity());

		JSONArray rootJsonArray = jsonData.toJsonArray();

		if (log.isLoggable(Level.FINEST)) {
			SignavioLogHelper.logJSONArray(log, rootJsonArray);
		}

		// find the directory of type public which contains all directories and models of this account
		for (int i = 0; i < rootJsonArray.length(); i++) {
			JSONObject rootObject = rootJsonArray.getJSONObject(i);

			if ("public".equals(rootObject.getJSONObject("rep").get("type"))) {
				return rootObject;
			}
		}

		if (log.isLoggable(Level.FINEST)) {
			SignavioLogHelper.logCookieAndBody(log, directoryResponse);
		}

		// TODO: Check -> I think the root should always exist! So throw an exception here (Bernd). @Christian: WDYT?
		throw new RepositoryException("No directory root found in signavio repository.");
		// return null;
	}

	private FolderInfo getFolderInfo(JSONObject jsonDirectoryObject) throws JSONException {
		if (!"dir".equals(jsonDirectoryObject.getString("rel"))) {
			// TODO: Think about that!
			throw new RepositoryException(jsonDirectoryObject + " is not a directory");
		}
		String directoryName = jsonDirectoryObject.getJSONObject("rep").getString("name");
		log.finest("Directoryname: " + directoryName);
		// String directoryDescription = jsonDirectoryObject.getJSONObject("rep").getString("description");
		String href = jsonDirectoryObject.getString("href");

		// for (JSONObject subDirectoryInfo : getSubDirectoryInfos(href)) {
		//    printDirectory(subDirectoryInfo, indention);
		// }
		//
		// for (JSONObject modelInfo : getSubModelInfos(href)) {
		//    String modelName = modelInfo.getJSONObject("rep").getString("name");
		//    String modelType = modelInfo.getJSONObject("rep").getString("type");
		//    System.out.println(indention + "- MODEL " + modelName + " (" + modelType + ")");
		// }

		FolderInfo folderInfo = new FolderInfo(this);
		// folderInfo.setId( directoryId );
		folderInfo.setName(directoryName);
		folderInfo.setPath(href);
		return folderInfo;
	}

	private FileInfo getFileInfo(JSONObject relObject) throws JSONException {
		FileInfo fileInfo = new FileInfo(this);
		fileInfo.setPath(relObject.getString("href"));
		log.finest("FileInfo-setPath: " + relObject.getString("href"));
		fileInfo.setName(relObject.getJSONObject("rep").getString("name"));
		// fileInfo.setId(id);
		// relObject.getJSONObject("rep").getInt("rev");

		String fileTypeIdentifier = relObject.getJSONObject("rep").getString("namespace");
		fileInfo.setFileType(FileTypeRegistry.getFileTypeByIdentifier(fileTypeIdentifier));
		// if ("BPMN 1.2".equals(relObject.getJSONObject("rep").getString("type"))) {
		// fileInfo.setFileType(FileTypeRegistry.getFileTyp(SIGNAVIO_BPMN_1_2));
		// }
		// else if ("BPMN 2.0".equals(relObject.getJSONObject("rep").getString("type"))) {
		// fileInfo.setFileType(FileTypeRegistry.getFileTyp(SIGNAVIO_BPMN_2_0));
		// }
		// relObject.getJSONObject("rep").getString("author");
		// relObject.getJSONObject("rep").getString("revision");
		// relObject.getJSONObject("rep").getString("description");
		// relObject.getJSONObject("rep").getString("comment");
		// relObject.getJSONObject("rep").getString("namespace");
		return fileInfo;

		// TODO: Add file actions here (jpdl4/bpmn20/png), maybe define an action factory which produces the concrete actions?
	}

	public FolderInfo getRootFolder() {
		try {
			return getFolderInfo(getPublicRootDirectory());
		} catch (Exception e) {
			throw new RepositoryException("Eror while accessing the Signavio Repository", e);
		}
	}

	public FolderInfo loadChildren(FolderInfo folder) {
		try {
			Client client = initClient();

			Reference directoryRef = new Reference(SERVER_URL + folder.getPath());

			Request directoryRequest = new Request(Method.GET, directoryRef);
			directoryRequest.getClientInfo().getAcceptedMediaTypes().add(new Preference<MediaType>(MediaType.APPLICATION_JSON));

			Form requestHeaders = new Form();
			requestHeaders.add("token", securityToken);
			directoryRequest.getAttributes().put("org.restlet.http.headers", requestHeaders);

			Response directoryResponse = client.handle(directoryRequest);
			JsonRepresentation jsonData = new JsonRepresentation(directoryResponse.getEntity());

			JSONArray relJsonArray = jsonData.toJsonArray();

			if (log.isLoggable(Level.FINEST)) {
				SignavioLogHelper.logJSONArray(log, relJsonArray);
			}
			for (int i = 0; i < relJsonArray.length(); i++) {
				JSONObject relObject = relJsonArray.getJSONObject(i);

				if ("dir".equals(relObject.getString("rel"))) {
					FolderInfo folderInfo = getFolderInfo(relObject);
					folder.getSubFolders().add(folderInfo);
				} else if ("mod".equals(relObject.getString("rel"))) {
					FileInfo fileInfo = getFileInfo(relObject);
					folder.getFiles().add(fileInfo);
				}
			}

			return folder;
		} catch (Exception ex) {
			throw new RepositoryException("Exception while accessing Signavio repository", ex);
		}
	}

	public void createNewFile(FolderInfo folderInfo, FileInfo file) {
	}

	public void createNewSubFolder(FolderInfo parentFolder, FolderInfo subFolder) {
	}

	public void deleteFile(FolderInfo folderInfo, FileInfo file) {
	}

	public void deleteSubFolder(FolderInfo parentFolder, FolderInfo subFolder) {
	}

	public String getSecurityToken() {
		return securityToken;
	}

	public void setSecurityToken(String securityToken) {
		this.securityToken = securityToken;
	}

	/*
	 * jpdl4 /model/175ded5335ee492f915b2341c0bc6a04/info rel:"info" rep->namespace:"http://b3mn.org/stencilset/jbpm4#" rep->type:"BPMN 1.2"
	 * rel:"exp" href:"/model/175ded5335ee492f915b2341c0bc6a04/jpdl4" rep->mime:"application/xml"
	 * png rel:"exp" href:"/model/175ded5335ee492f915b2341c0bc6a04/png" rep->mime:"image/png"
	 * rdf rel:"exp" href:"/model/175ded5335ee492f915b2341c0bc6a04/rdf" rep->mime:"application/rdf+xml"
	 */
	public JSONObject getModelAsJsonRepresentation(FileInfo fileInfo) {
		try {
			Client client = initClient();
			// TODO: what to add here to get json out of signavio? have to reengineer signavio rest mechanism
			Reference jsonRef = new Reference(SERVER_URL + fileInfo.getPath() + "/json");

			Request jsonRequest = new Request(Method.GET, jsonRef);
			jsonRequest.getClientInfo().getAcceptedMediaTypes().add(new Preference<MediaType>(MediaType.APPLICATION_JSON));

			Form requestHeaders = new Form();
			requestHeaders.add("token", securityToken);
			jsonRequest.getAttributes().put("org.restlet.http.headers", requestHeaders);

			Response jsonResponse = client.handle(jsonRequest);

			return new JSONObject(jsonResponse.getEntity().getText());
		} catch (Exception ex) {
			// TODO: No json representation available, wtd?
			throw new RepositoryException("Error while accessing Signavio repository", ex);
		}
	}

	public String getModelAsJpdl4Representation(FileInfo fileInfo) {
		try {
			Client client = initClient();
			Reference jpdlRef = new Reference(SERVER_URL + fileInfo.getPath() + "/jpdl4");

			Request jpdlRequest = new Request(Method.GET, jpdlRef);
			jpdlRequest.getClientInfo().getAcceptedMediaTypes().add(new Preference<MediaType>(MediaType.APPLICATION_XML));

			Form requestHeaders = new Form();
			requestHeaders.add("token", securityToken);
			jpdlRequest.getAttributes().put("org.restlet.http.headers", requestHeaders);

			Response jpdlResponse = client.handle(jpdlRequest);
			DomRepresentation xmlData = jpdlResponse.getEntityAsDom();

			StringWriter stringWriter = new StringWriter();
			StreamResult streamResult = new StreamResult(stringWriter);
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.transform(xmlData.getDomSource(), streamResult);

			String result = stringWriter.toString();
			log.finest("JPDL4 String: " + result);

			return result;
		} catch (Exception ex) {
			// TODO: No jpdl4 representation available, wtd?
			throw new RepositoryException("Exception while accessing Signavio repository", ex);
		}
	}

	public String getModelAsBpmn20Representation(FileInfo fileInfo) {

		try {
			Client client = initClient();
			Reference bpmn20Ref = new Reference(SERVER_URL + fileInfo.getPath() + "/bpmn2_0_xml");

			Request bpmn20Request = new Request(Method.GET, bpmn20Ref);
			bpmn20Request.getClientInfo().getAcceptedMediaTypes().add(new Preference<MediaType>(MediaType.APPLICATION_XML));

			Form requestHeaders = new Form();
			requestHeaders.add("token", securityToken);
			bpmn20Request.getAttributes().put("org.restlet.http.headers", requestHeaders);

			Response bpmn20Response = client.handle(bpmn20Request);
			DomRepresentation xmlData = bpmn20Response.getEntityAsDom();

			StringWriter stringWriter = new StringWriter();
			StreamResult streamResult = new StreamResult(stringWriter);
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.transform(xmlData.getDomSource(), streamResult);

			String result = stringWriter.toString();
			log.finest("BPMN2.0 String: " + result);

			return result;
		} catch (Exception ex) {
			// TODO: No bpmn20 representation available, wtd?
			throw new RepositoryException("Exception while accessing Signavio repository", ex);
		}
	}

	public byte[] getModelAsPngRepresentation(FileInfo fileInfo) {
		try {
			Client client = initClient();
			Reference pngRef = new Reference(SERVER_URL + fileInfo.getPath() + "/png");

			Request pngRequest = new Request(Method.GET, pngRef);
			pngRequest.getClientInfo().getAcceptedMediaTypes().add(new Preference<MediaType>(MediaType.IMAGE_PNG));

			Form requestHeaders = new Form();
			requestHeaders.add("token", securityToken);
			pngRequest.getAttributes().put("org.restlet.http.headers", requestHeaders);

			Response pngResponse = client.handle(pngRequest);
			Representation imageData = pngResponse.getEntity();

			String text = imageData.getText();
			byte[] result = text.getBytes();

			if (log.isLoggable(Level.FINEST)) {
			  log.finest("PNG - byte result: " + new String(result, "UTF-8"));
			}

			return result;

		} catch (Exception ex) {
			// TODO: No png representation available, wtd?
			throw new RepositoryException("Exception while accessing Signavio repository", ex);
		}
	}

	public String getModelAsPngUrl(FileInfo fileInfo) {
		return SERVER_URL + fileInfo.getPath() + "/png?token=" + getSecurityToken();
	}

	public String getModellerUrl(FileInfo fileInfo) {
		// substring 7 to remove prefix_string '/model/' return from getPath() to get raw model id
		return EDITOR_URL + "?id=" + fileInfo.getPath().substring(7);
	}

	public String getModelUrl(FileInfo fileInfo) {
		return SERVER_URL + fileInfo.getPath();
	}

	public JSONArray getEmbeddedModel(FileInfo fileInfo) {
		try {
			Client client = initClient();

			Reference embeddedModelRef = new Reference(SERVER_URL + "/purl");

			// Create POST parameters
			Form embeddedModelForm = new Form();
			embeddedModelForm.add("label", "");
			embeddedModelForm.add("mails", "");
			embeddedModelForm.add("message", "");
			embeddedModelForm.add("sbo", fileInfo.getPath().substring(7));
			embeddedModelForm.add("type", "png");
			Representation embeddedModelRep = embeddedModelForm.getWebRepresentation();

			// Create Request
			Request embeddedBORequest = new Request(Method.POST, embeddedModelRef, embeddedModelRep);
			// Set MediaType to retrieve JSON data
			embeddedBORequest.getClientInfo().getAcceptedMediaTypes().add(new Preference<MediaType>(MediaType.APPLICATION_JSON));

			// Add the securityToken to the Request Headers
			Form requestHeaders = new Form();
			requestHeaders.add("token", securityToken);
			embeddedBORequest.getAttributes().put("org.restlet.http.headers", requestHeaders);

			// Send the request and retrieve the response
			Response embeddedBOResponse = client.handle(embeddedBORequest);

			// Get the ResponseBody as JSON
			JsonRepresentation jsonData = new JsonRepresentation(embeddedBOResponse.getEntity());

			// Transform to JSONArray
			JSONArray jsonArray = jsonData.toJsonArray();

			// Content of jsonArray above with modelID /model/6fd6be02c610475c9daab28a046282e2
			// [
			// {
			// "rel":"purl",
			// "href":"/purl/6fd6be02c610475c9daab28a046282e2/info/f5f981a520574e9e9404e2cd09473d93",
			// "rep":
			// {
			// "id":"f5f981a520574e9e9404e2cd09473d93",
			// "authkey":"f32447ff8d073af5373542f6b6fb8b265b68ed71d6a1b9c5415fdf522d430",
			// "sbo":"6fd6be02c610475c9daab28a046282e2",
			// "label":"",
			// "type":"png",
			// "uri":"/p/model/6fd6be02c610475c9daab28a046282e2/png?inline"
			// }
			// },
			// {
			// "rel":"purl",
			// "href":"/purl/6fd6be02c610475c9daab28a046282e2/info/6fe6c5f6360d4d5983c538d7001b02c2",
			// "rep":
			// {
			// "id":"6fe6c5f6360d4d5983c538d7001b02c2",
			// "authkey":"c42e3bc5e57d7d2121156d16e1e1896d0c153fb5ac29f1e78f177628243f97",
			// "sbo":"6fd6be02c610475c9daab28a046282e2",
			// "label":"",
			// "type":"json",
			// "uri":"/p/model/6fd6be02c610475c9daab28a046282e2/json?inline"
			// }
			// },
			// {
			// "rel":"purl",
			// "href":"/purl/6fd6be02c610475c9daab28a046282e2/info/64a2f892812942ad98a6e70d790b862d",
			// "rep":
			// {
			// "id":"64a2f892812942ad98a6e70d790b862d",
			// "authkey":"bb43cdb6d6c8cb568c3fbcc567e7d521ca5f9e74d0cfe39813e62cce7d88c61",
			// "sbo":"6fd6be02c610475c9daab28a046282e2",
			// "label":"",
			// "type":"stencilset",
			// "uri":"/p/editor_stencilset"
			// }
			// },
			// {
			// "rel":"purl",
			// "href":"/purl/6fd6be02c610475c9daab28a046282e2/info/d59c157675c0476f9b6279b8ee97105b",
			// "rep":
			// {
			// "id":"d59c157675c0476f9b6279b8ee97105b",
			// "authkey":"eb19dcfedba71d9f7daefeb4188ad72daeec46539926bc6fd3e43a5ddd31d7",
			// "sbo":"6fd6be02c610475c9daab28a046282e2",
			// "label":"",
			// "type":"gadget",
			// "uri":"/p/model/6fd6be02c610475c9daab28a046282e2/gadget"
			// }
			// }
			// ]

			// this should be the return value of the method

			// <script type="text/javascript" src="http://127.0.0.1:8080/mashup/signavio.js"></script>
			// <script type="text/plain">
			// {
			// url: "http://127.0.0.1:8080/p/model/6fd6be02c610475c9daab28a046282e2",
			// authToken:
			// "f32447ff8d073af5373542f6b6fb8b265b68ed71d6a1b9c5415fdf522d430_c42e3bc5e57d7d2121156d16e1e1896d0c153fb5ac29f1e78f177628243f97_bb43cdb6d6c8cb568c3fbcc567e7d521ca5f9e74d0cfe39813e62cce7d88c61",
			// overflowX: "fit",
			// overflowY: "fit",
			// <!-- begin optional ------
			// width: 200,
			// height: 200,
			// ---- end optional ------->
			// zoomSlider: true,
			// linkSubProcesses: false
			// }
			// </script>

			return jsonArray;
		} catch (Exception ex) {
			throw new RepositoryException("Exception while retrieving embeddedModel from Signavio", ex);
		}
	}

	public void deleteEmbeddedModel(FileInfo fileInfo) {
		try {
			Client client = initClient();

			Reference embeddedModelRef = new Reference(SERVER_URL + "/purl/" + fileInfo.getPath().substring(7) + "/info/");

			Request embeddedModelRequest = new Request(Method.DELETE, embeddedModelRef);

//  not needed!
//			Form embeddedModelForm = new Form();
//			embeddedModelForm.add("label", "");
//			embeddedModelForm.add("mails", "");
//			embeddedModelForm.add("message", "");
//			embeddedModelForm.add("sbo", fileInfo.getPath().substring(7));
//			embeddedModelForm.add("type", "png");
//			Representation embeddedBORep = embeddedModelForm.getWebRepresentation();

			Form requestHeaders = new Form();
			requestHeaders.add("token", securityToken);
			embeddedModelRequest.getAttributes().put("org.restlet.http.headers", requestHeaders);

			client.handle(embeddedModelRequest);

		} catch (Exception ex) {
			throw new RepositoryException("Exception while accessing Signavio repository", ex);
		}
	}

  public List<FolderInfo> getChildFolders(FolderInfo parentFolder) {
    
    return null;
  }
}
