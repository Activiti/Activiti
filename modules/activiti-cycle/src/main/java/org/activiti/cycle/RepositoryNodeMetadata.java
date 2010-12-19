package org.activiti.cycle;

import java.util.Date;
import java.util.Map;

/**
 * Metadata about a {@link RepositoryNode}
 * 
 * @author bernd.ruecker@camunda.com
 */
public interface RepositoryNodeMetadata {

	/**
	 * name of artifact for showing to the user (no unique identifier!)
	 */
	public String getName();
	

	/**
	 * The id of the parent {@link RepositoryFolder}
	 */
  public String getParentFolderId();
  
	public Date getCreated();
  public Date getLastChanged();
  public String getAuthor();
  public String getLastAuthor();
  public String getVersion();

	/**
	 * get the metadata as Map containing all attributes as String entries
	 * which can be easily used for a GUI to show the attributes generically
	 */
	public Map<String, String> getAsStringMap();

  public void setName(String name);
  public void setParentFolderId(String id);

  public void setCreated(Date created);
  public void setLastChanged(Date lastChanged);
  public void setAuthor(String author);
  public void setLastAuthor(String lastAuthor);
  public void setVersion(String version);

}
