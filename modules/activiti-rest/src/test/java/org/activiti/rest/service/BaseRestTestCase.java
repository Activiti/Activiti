package org.activiti.rest.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;

import junit.framework.AssertionFailedError;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.jobexecutor.JobExecutor;
import org.activiti.engine.impl.test.PvmTestCase;
import org.activiti.engine.impl.test.TestHelper;
import org.activiti.engine.impl.util.ClockUtil;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.rest.service.application.ActivitiRestServicesApplication;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.util.ISO8601Utils;
import org.codehaus.jackson.node.ObjectNode;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Assert;
import org.restlet.Component;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.engine.header.Header;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.util.Series;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseRestTestCase extends PvmTestCase {

  private static Logger log = LoggerFactory.getLogger(BaseRestTestCase.class);
  protected Component component;
  protected ObjectMapper objectMapper = new ObjectMapper();
  
  private static final List<String> TABLENAMES_EXCLUDED_FROM_DB_CLEAN_CHECK = Arrays.asList(
      "ACT_GE_PROPERTY"
    );

  protected ProcessEngine processEngine;
  protected static ProcessEngine cachedProcessEngine;
  
  protected String deploymentId;
  protected Throwable exception;

  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected RepositoryService repositoryService;
  protected RuntimeService runtimeService;
  protected TaskService taskService;
  protected FormService formService;
  protected HistoryService historyService;
  protected IdentityService identityService;
  protected ManagementService managementService;
  
  protected ClientResource getAuthenticatedClient(String uri) {
    ClientResource client = new ClientResource("http://localhost:8182/" + uri);
    client.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "kermit", "kermit");
    return client;
  }
  
  protected void initializeProcessEngine() {
    if (cachedProcessEngine==null) {
      cachedProcessEngine = ProcessEngineConfiguration.createProcessEngineConfigurationFromResource("activiti.cfg.xml").buildProcessEngine();
      if (cachedProcessEngine==null) {
        throw new ActivitiException("no in-memory process engine available");
      }
      // hack to circumvent the loading of the activiti-context.xml of the REST web application
      ProcessEnginesRest.init();
      ProcessEngines.registerProcessEngine(cachedProcessEngine);
    }
    processEngine = cachedProcessEngine;
  }
  
  @Override
  public void runBare() throws Throwable {
    initializeRestServer();
    initializeProcessEngine();
    if (repositoryService==null) {
      initializeServices();
    }
    
    createUsers();

    log.error(EMPTY_LINE);

    try {
      
      deploymentId = TestHelper.annotationDeploymentSetUp(processEngine, getClass(), getName());
      
      super.runBare();

    } catch (AssertionFailedError e) {
      log.error(EMPTY_LINE);
      log.error("ASSERTION FAILED: {}", e, e);
      exception = e;
      throw e;
      
    } catch (Throwable e) {
      log.error(EMPTY_LINE);
      log.error("EXCEPTION: {}", e, e);
      exception = e;
      throw e;
      
    } finally {
      TestHelper.annotationDeploymentTearDown(processEngine, deploymentId, getClass(), getName());
      dropUsers();
      assertAndEnsureCleanDb();
      stopRestServer();
      ClockUtil.reset();
    }
  }
  
  protected void createUsers() {
    IdentityService identityService = processEngine.getIdentityService();
    User user = identityService.newUser("kermit");
    user.setFirstName("Kermit");
    user.setLastName("the Frog");
    user.setPassword("kermit");
    identityService.saveUser(user);
    
    Group group = identityService.newGroup("admin");
    group.setName("Administrators");
    identityService.saveGroup(group);
    
    identityService.createMembership(user.getId(), group.getId());
  }
  
  protected void initializeRestServer() throws Exception {
    component = new Component();  
    // Add a new HTTP server listening on port 8182.  
    component.getServers().add(Protocol.HTTP, 8182);   
    component.getDefaultHost().attach(new ActivitiRestServicesApplication());
    component.start();
  }
  
  protected void dropUsers() {
    IdentityService identityService = processEngine.getIdentityService();
    
    identityService.deleteUser("kermit");
    identityService.deleteGroup("admin");
    identityService.deleteMembership("kermit", "admin");
  }
  
  protected void stopRestServer() throws Exception {
    component.stop();
  }
  
  /** Each test is assumed to clean up all DB content it entered.
   * After a test method executed, this method scans all tables to see if the DB is completely clean. 
   * It throws AssertionFailed in case the DB is not clean.
   * If the DB is not clean, it is cleaned by performing a create a drop. */
  protected void assertAndEnsureCleanDb() throws Throwable {
    log.debug("verifying that db is clean after test");
    Map<String, Long> tableCounts = managementService.getTableCount();
    StringBuilder outputMessage = new StringBuilder();
    for (String tableName : tableCounts.keySet()) {
      String tableNameWithoutPrefix = tableName.replace(processEngineConfiguration.getDatabaseTablePrefix(), "");
      if (!TABLENAMES_EXCLUDED_FROM_DB_CLEAN_CHECK.contains(tableNameWithoutPrefix)) {
        Long count = tableCounts.get(tableName);
        if (count!=0L) {
          outputMessage.append("  "+tableName + ": " + count + " record(s) ");
        }
      }
    }
    if (outputMessage.length() > 0) {
      outputMessage.insert(0, "DB NOT CLEAN: \n");
      log.error(EMPTY_LINE);
      log.error(outputMessage.toString());
      
      log.info("dropping and recreating db");
      
      CommandExecutor commandExecutor = ((ProcessEngineImpl)processEngine).getProcessEngineConfiguration().getCommandExecutor();
      commandExecutor.execute(new Command<Object>() {
        public Object execute(CommandContext commandContext) {
          DbSqlSession session = commandContext.getSession(DbSqlSession.class);
          session.dbSchemaDrop();
          session.dbSchemaCreate();
          return null;
        }
      });

      if (exception!=null) {
        throw exception;
      } else {
        Assert.fail(outputMessage.toString());
      }
    } else {
      log.info("database was clean");
    }
  }
  
  protected String encode(String string) {
    if(string != null) {
      try {
        return URLEncoder.encode(string, "UTF-8");
      } catch (UnsupportedEncodingException uee) {
        throw new IllegalStateException("JVM does not support UTF-8 encoding.", uee);
      }
    }
    return null;
  }


  protected void initializeServices() {
    processEngineConfiguration = ((ProcessEngineImpl) processEngine).getProcessEngineConfiguration();
    repositoryService = processEngine.getRepositoryService();
    runtimeService = processEngine.getRuntimeService();
    taskService = processEngine.getTaskService();
    formService = processEngine.getFormService();
    historyService = processEngine.getHistoryService();
    identityService = processEngine.getIdentityService();
    managementService = processEngine.getManagementService();
  }
  
  public void assertProcessEnded(final String processInstanceId) {
    ProcessInstance processInstance = processEngine
      .getRuntimeService()
      .createProcessInstanceQuery()
      .processInstanceId(processInstanceId)
      .singleResult();
    
    if (processInstance!=null) {
      throw new AssertionFailedError("Expected finished process instance '"+processInstanceId+"' but it was still in the db"); 
    }
  }

  public void waitForJobExecutorToProcessAllJobs(long maxMillisToWait, long intervalMillis) {
    JobExecutor jobExecutor = processEngineConfiguration.getJobExecutor();
    jobExecutor.start();

    try {
      Timer timer = new Timer();
      InteruptTask task = new InteruptTask(Thread.currentThread());
      timer.schedule(task, maxMillisToWait);
      boolean areJobsAvailable = true;
      try {
        while (areJobsAvailable && !task.isTimeLimitExceeded()) {
          Thread.sleep(intervalMillis);
          areJobsAvailable = areJobsAvailable();
        }
      } catch (InterruptedException e) {
      } finally {
        timer.cancel();
      }
      if (areJobsAvailable) {
        throw new ActivitiException("time limit of " + maxMillisToWait + " was exceeded");
      }

    } finally {
      jobExecutor.shutdown();
    }
  }

  public void waitForJobExecutorOnCondition(long maxMillisToWait, long intervalMillis, Callable<Boolean> condition) {
    JobExecutor jobExecutor = processEngineConfiguration.getJobExecutor();
    jobExecutor.start();

    try {
      Timer timer = new Timer();
      InteruptTask task = new InteruptTask(Thread.currentThread());
      timer.schedule(task, maxMillisToWait);
      boolean conditionIsViolated = true;
      try {
        while (conditionIsViolated) {
          Thread.sleep(intervalMillis);
          conditionIsViolated = !condition.call();
        }
      } catch (InterruptedException e) {
      } catch (Exception e) {
        throw new ActivitiException("Exception while waiting on condition: "+e.getMessage(), e);
      } finally {
        timer.cancel();
      }
      if (conditionIsViolated) {
        throw new ActivitiException("time limit of " + maxMillisToWait + " was exceeded");
      }

    } finally {
      jobExecutor.shutdown();
    }
  }

  public boolean areJobsAvailable() {
    return !managementService
      .createJobQuery()
      .executable()
      .list()
      .isEmpty();
  }

  private static class InteruptTask extends TimerTask {
    protected boolean timeLimitExceeded = false;
    protected Thread thread;
    public InteruptTask(Thread thread) {
      this.thread = thread;
    }
    public boolean isTimeLimitExceeded() {
      return timeLimitExceeded;
    }
    public void run() {
      timeLimitExceeded = true;
      thread.interrupt();
    }
  }
  
  /**
   * Checks if the returned "data" array (child-node of root-json node returned by invoking a GET on the given url) 
   * contains entries with the given ID's.
   */
  protected void assertResultsPresentInDataResponse(String url, String... expectedResourceIds) throws JsonProcessingException, IOException {
    int numberOfResultsExpected = expectedResourceIds.length;
    
    // Do the actual call
    ClientResource client = getAuthenticatedClient(url);
    Representation response = client.get();
    
    // Check status and size
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
    JsonNode dataNode = objectMapper.readTree(response.getStream()).get("data");
    assertEquals(numberOfResultsExpected, dataNode.size());

    // Check presence of ID's
    List<String> toBeFound = new ArrayList<String>(Arrays.asList(expectedResourceIds));
    Iterator<JsonNode> it = dataNode.iterator();
    while(it.hasNext()) {
      String id = it.next().get("id").getTextValue();
      toBeFound.remove(id);
    }
    assertTrue("Not all process-definitions have been found in result, missing: " + StringUtils.join(toBeFound, ", "), toBeFound.isEmpty());
    
    client.release();
  }
  
  /**
   * Checks if the returned "data" array (child-node of root-json node returned by invoking a POST on the given url) 
   * contains entries with the given ID's.
   */
  protected void assertResultsPresentInDataResponse(String url, ObjectNode body, String... expectedResourceIds) throws JsonProcessingException, IOException {
    int numberOfResultsExpected = 0;
    if (expectedResourceIds != null) {
      numberOfResultsExpected = expectedResourceIds.length;
    }
    
    // Do the actual call
    ClientResource client = getAuthenticatedClient(url);
    Representation response = client.post(body);
    
    // Check status and size
    assertEquals(Status.SUCCESS_OK, client.getResponse().getStatus());
    JsonNode rootNode = objectMapper.readTree(response.getStream());
    JsonNode dataNode = rootNode.get("data");
    assertEquals(numberOfResultsExpected, dataNode.size());

    // Check presence of ID's
    if (expectedResourceIds != null) {
      List<String> toBeFound = new ArrayList<String>(Arrays.asList(expectedResourceIds));
      Iterator<JsonNode> it = dataNode.iterator();
      while(it.hasNext()) {
        String id = it.next().get("id").getTextValue();
        toBeFound.remove(id);
      }
      assertTrue("Not all entries have been found in result, missing: " + StringUtils.join(toBeFound, ", "), toBeFound.isEmpty());
    }
    
    client.release();
  }
  
  /**
   * Checks if the rest operation returns an error as expected 
   */
  protected void assertErrorResult(String url, ObjectNode body, Status status) throws IOException {
    
    // Do the actual call
    ClientResource client = getAuthenticatedClient(url);
    try {
      client.post(body);
      fail();
    } catch(Exception e) {
      // Check status
      assertEquals(status, client.getResponse().getStatus());
    }
    
    client.release();
  }
  
  /**
   * Extract a date from the given string. Assertion fails when invalid date has been provided.
   */
  protected Date getDateFromISOString(String isoString) {
    DateTimeFormatter dateFormat = ISODateTimeFormat.dateTime();
    try {
      return dateFormat.parseDateTime(isoString).toDate();
    } catch(IllegalArgumentException iae) {
      fail("Illegal date provided: "+ isoString);
      return null;
    }
  }
  
  protected String getISODateString(Date time) {
    return ISO8601Utils.format(time, true);
  }
  
  protected String getMediaType(ClientResource client) {
    @SuppressWarnings("unchecked")
    Series<Header> headers = (Series<Header>) client.getResponseAttributes().get(HeaderConstants.ATTRIBUTE_HEADERS);
    return headers.getFirstValue(HeaderConstants.HEADER_CONTENT_TYPE);
  }
}
