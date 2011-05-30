package org.activiti.cycle.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.activiti.cycle.RepositoryNode;
import org.activiti.cycle.RepositoryNodeMetadata;

/**
 * Metadata about a {@link RepositoryNode}
 * 
 * @author bernd.ruecker@camunda.com
 */
public class RepositoryNodeMetadataImpl implements RepositoryNodeMetadata {

	/**
	 * name of artifact for showing to the user (no unique identifier!)
	 */
	private String name;
	
	/**
	 * String information  on path (important e.g. in Signavio,
	 * since the path is not contained in the URL).
	 * 
	 * Note that this is as default a read-only property provided
	 * as additional information for the GUI.
	 */
	private String folderId;	

	private Date created;

	private Date lastChanged;
	
	private String author;
	
	private String lastAuthor;
	
	private String version;
	 // ...?

	private Map<String, String> additionalMetadata = new HashMap<String, String>();

	/**
	 * get the metadata as Map containing all attributes as String entries
	 * which can be easily used for a GUI to show the attributes generically
	 */
	public Map<String, String> getAsStringMap() {
		HashMap<String,String> map = new HashMap<String, String>();
		
		// TODO: Change to use reflection?
		// TODO: DateFormatter?
		// TODO: Check if NULL should be excluded from Map
		map.put("name", name);
		map.put("path", folderId);
		map.put("created", String.valueOf( created ));
		map.put("lastChanged", String.valueOf(lastChanged));
		map.put("author", author);
		map.put("lastAuthor", lastAuthor);
		map.put("version", String.valueOf(version));
		
		map.putAll(additionalMetadata);
		
		return map;
	}
	
	public String getMetadata(String key) {
	  if (additionalMetadata.containsKey(key)) {
	    return additionalMetadata.get(key);
	  }
	  else {
	    // maybe we requested some of the prepared attributes
	    // otherwise the default is NULL automatically
	    return getAsStringMap().get(key);
	  }
	}

  public boolean hasMetadata(String key) {
    boolean result = additionalMetadata.containsKey(key);
    if (result) {
      return true;
    }
    else {
      return getAsStringMap().containsKey(key);
    }
  }

  public void setAdditionalMetadata(String key, String value) {
	    additionalMetadata.put(key, value);
	}

  public String toString() {
    return "{name=" + name + ";path=" + folderId + ";version=" + version + "}";
  }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getParentFolderId() {
    return folderId;
	}

	public void setParentFolderId(String folderId) {
    this.folderId = folderId;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getLastChanged() {
		return lastChanged;
	}

	public void setLastChanged(Date lastChanged) {
		this.lastChanged = lastChanged;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getLastAuthor() {
		return lastAuthor;
	}

	public void setLastAuthor(String lastAuthor) {
		this.lastAuthor = lastAuthor;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

}
