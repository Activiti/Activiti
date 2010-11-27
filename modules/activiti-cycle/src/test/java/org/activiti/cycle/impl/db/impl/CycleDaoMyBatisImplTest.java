package org.activiti.cycle.impl.db.impl;

import java.util.List;

import org.activiti.cycle.impl.CycleTagContentImpl;
import org.activiti.cycle.impl.db.CycleDAO;
import org.activiti.cycle.impl.db.entity.RepositoryArtifactLinkEntity;
import org.activiti.cycle.impl.db.entity.RepositoryNodePeopleLinkEntity;
import org.activiti.cycle.impl.db.entity.RepositoryNodeTagEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;


public class CycleDaoMyBatisImplTest extends PluggableActivitiTestCase {
  
  private CycleDAO dao;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
//    if (ProcessEngines.getDefaultProcessEngine() == null) {
//      initializeProcessEngine();
//      ProcessEngines.registerProcessEngine(processEngine);
//    }
    dao = new CycleDaoMyBatisImpl();
  }
  
  public void testArtifactLinks() throws Throwable {    
    RepositoryArtifactLinkEntity link = new RepositoryArtifactLinkEntity();
    link.setSourceConnectorId("connector1");
    link.setSourceArtifactId("artifact1");
    link.setTargetConnectorId("connector2");
    link.setTargetArtifactId("artifact2");
    link.setLinkType("TEST");
    link.setComment("Created in test case");
    link.setLinkedBothWays(true);
    
    dao.insertArtifactLink(link);
    
    List<RepositoryArtifactLinkEntity> links1 = dao.getOutgoingArtifactLinks("connector1", "artifact1");
    assertEquals(1, links1.size());
    assertEquals("Created in test case", links1.get(0).getComment());
    assertNull(links1.get(0).getSourceRevision());
    assertNotNull(links1.get(0).getId());

    List<RepositoryArtifactLinkEntity> links2 = dao.getOutgoingArtifactLinks("connector2", "artifact2");
    assertEquals(0, links2.size());

    List<RepositoryArtifactLinkEntity> links3 = dao.getIncomingArtifactLinks("connector1", "artifact1");
    assertEquals(0, links3.size());

    List<RepositoryArtifactLinkEntity> links4 = dao.getIncomingArtifactLinks("connector2", "artifact2");
    assertEquals(1, links4.size());
    assertEquals("Created in test case", links4.get(0).getComment());
    assertEquals(links1.get(0).getId(), links4.get(0).getId());
    
    dao.deleteArtifactLink(links1.get(0).getId());

    List<RepositoryArtifactLinkEntity> links5 = dao.getIncomingArtifactLinks("connector2", "artifact2");
    assertEquals(0, links5.size());
  }
  
  public void testPeopleLink() {
    RepositoryNodePeopleLinkEntity link = new RepositoryNodePeopleLinkEntity();
    link.setSourceConnectorId("connector1");
    link.setSourceArtifactId("artifact1");
    link.setLinkType("TEST");
    link.setComment("Created in test case");
    link.setUserId("bernd");

    dao.insertPeopleLink(link);

    List<RepositoryNodePeopleLinkEntity> links1 = dao.getPeopleLinks("connector1", "artifact1");
    assertEquals(1, links1.size());
    assertEquals("Created in test case", links1.get(0).getComment());
    assertNotNull(links1.get(0).getId());

    List<RepositoryNodePeopleLinkEntity> links2 = dao.getPeopleLinks("connector2", "artifact2");
    assertEquals(0, links2.size());

    dao.deletePeopleLink(links1.get(0).getId());

    List<RepositoryNodePeopleLinkEntity> links3 = dao.getPeopleLinks("connector2", "artifact2");
    assertEquals(0, links3.size());
  }

  public void testTag() {
    RepositoryNodeTagEntity tag = new RepositoryNodeTagEntity("name1", "connector1", "artifact1");
    dao.insertTag(tag);
    
    List<RepositoryNodeTagEntity> tags1 = dao.getTagsForNode("connector1", "artifact1");
    assertEquals(1, tags1.size());
    assertEquals("name1", tags1.get(0).getName());
    assertNotNull(tags1.get(0).getId());
    
    List<RepositoryNodeTagEntity> tags2 = dao.getTagsForNode("connector2", "artifact2");
    assertEquals(0, tags2.size());
    
    CycleTagContentImpl tagContent1 = dao.getTagContent("name1");
    assertEquals("name1", tagContent1.getName());
    assertEquals(1, tagContent1.getUsageCount());

    RepositoryNodeTagEntity tag2 = new RepositoryNodeTagEntity("name1", "connector1", "artifact2");
    dao.insertTag(tag2);
    RepositoryNodeTagEntity tag3 = new RepositoryNodeTagEntity("name2", "connector1", "artifact2");
    dao.insertTag(tag3);

    CycleTagContentImpl tagContent2 = dao.getTagContent("name1");
    assertEquals("name1", tagContent2.getName());
    assertEquals(2, tagContent2.getUsageCount());

    List<CycleTagContentImpl> tagsGroupedByName = dao.getTagsGroupedByName();
    assertEquals("name1", tagsGroupedByName.get(0).getName());
    assertEquals(2, tagsGroupedByName.get(0).getUsageCount());
    assertEquals("name2", tagsGroupedByName.get(1).getName());
    assertEquals(1, tagsGroupedByName.get(1).getUsageCount());

    dao.deleteTag("connector1", "artifact1", "name1");
    dao.deleteTag("connector1", "artifact2", "name1");
    dao.deleteTag("connector1", "artifact2", "name2");

    List<RepositoryNodeTagEntity> tags3 = dao.getTagsForNode("connector1", "artifact1");
    assertEquals(0, tags3.size());    

    // List<String> similiarTagNames = dao.getSimiliarTagNames("ame");
    // assertEquals(2, similiarTagNames.size());
    // assertEquals("name1", similiarTagNames.get(0));
    // assertEquals("name2", similiarTagNames.get(1));
  }

  public void testTagFindSimiliar() {
    RepositoryNodeTagEntity tag = new RepositoryNodeTagEntity("name1", "connector1", "artifact1");
    dao.insertTag(tag);

    List<RepositoryNodeTagEntity> tags1 = dao.getTagsForNode("connector1", "artifact1");
    assertEquals(1, tags1.size());
    assertEquals("name1", tags1.get(0).getName());
    assertNotNull(tags1.get(0).getId());

    List<RepositoryNodeTagEntity> tags2 = dao.getTagsForNode("connector2", "artifact2");
    assertEquals(0, tags2.size());

    CycleTagContentImpl tagContent1 = dao.getTagContent("name1");
    assertEquals("name1", tagContent1.getName());
    assertEquals(1, tagContent1.getUsageCount());

    RepositoryNodeTagEntity tag2 = new RepositoryNodeTagEntity("name1", "connector1", "artifact2");
    dao.insertTag(tag2);
    RepositoryNodeTagEntity tag3 = new RepositoryNodeTagEntity("name2", "connector1", "artifact2");
    dao.insertTag(tag3);

    CycleTagContentImpl tagContent2 = dao.getTagContent("name1");
    assertEquals("name1", tagContent2.getName());
    assertEquals(2, tagContent2.getUsageCount());

    List<CycleTagContentImpl> tagsGroupedByName = dao.getTagsGroupedByName();
    assertEquals("name1", tagsGroupedByName.get(0).getName());
    assertEquals(2, tagsGroupedByName.get(0).getUsageCount());
    assertEquals("name2", tagsGroupedByName.get(1).getName());
    assertEquals(1, tagsGroupedByName.get(1).getUsageCount());

    dao.deleteTag("connector1", "artifact1", "name1");
    dao.deleteTag("connector1", "artifact2", "name1");
    dao.deleteTag("connector1", "artifact2", "name2");

    List<RepositoryNodeTagEntity> tags3 = dao.getTagsForNode("connector1", "artifact1");
    assertEquals(0, tags3.size());
  }
  
  public void testComment() {

  }

}
