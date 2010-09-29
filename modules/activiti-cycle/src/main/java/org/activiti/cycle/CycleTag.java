package org.activiti.cycle;

import java.util.List;

public class CycleTag {

  /**
   * Artificial id for database (actually not sure if we need it, the tag should
   * be unique by name and alias.
   */
  private long id;

  private String name;
  private String alias;

  /**
   * all nodes having this tag. Actually not yet sure how that can be best
   * persisted to the database, maybe we could use a comma separated string
   * instead of a list?
   */
  private List<String> nodeIds;

  /**
   * returns the name maybe shown in the GUI for this tag (which is the alias if
   * present or the name otherwise)
   */
  public String getGuiLabel() {
    if (alias != null) {
      return alias;
    } else {
      return name;
    }
  }
  
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getAlias() {
    return alias;
  }

  public void setAlias(String alias) {
    this.alias = alias;
  }

  /**
   * returns a {@link List} of Strings containing all node ids which have this
   * tag (name and alias make a tag unique)
   */
  public List<String> getNodeIds() {
    return nodeIds;
  }

  public void setNodeIds(List<String> nodeIds) {
    this.nodeIds = nodeIds;
  }
  
}
