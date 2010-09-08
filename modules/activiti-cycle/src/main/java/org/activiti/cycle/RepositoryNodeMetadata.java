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
   * String information on path (important e.g. in Signavio, since the path is
   * not contained in the URL).
   * 
   * Note that this is as default a read-only property provided as additional
   * information for the GUI.
   */
  public String setParentFolderId();  

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
