package org.activiti.rest.api.jpa;

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
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.rest.JPAWebConfigurer;
import org.activiti.rest.api.jpa.repository.MessageRepository;
import org.activiti.rest.conf.JPAApplicationConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;

public class BaseJPARestTestCase extends PvmTestCase {

    private static Logger log = LoggerFactory.getLogger(BaseJPARestTestCase.class);

    protected static final int HTTP_SERVER_PORT = 7979;
    protected static final String SERVER_URL_PREFIX = "http://localhost:7979/service/";
    protected static final List<String> TABLENAMES_EXCLUDED_FROM_DB_CLEAN_CHECK = Arrays.asList(
            "ACT_GE_PROPERTY"
    );

    protected static Server server;
    protected static ApplicationContext appContext;
    protected ObjectMapper objectMapper = new ObjectMapper();

    protected static ProcessEngine processEngine;

    protected String deploymentId;
    protected Throwable exception;

    protected static ProcessEngineConfigurationImpl processEngineConfiguration;
    protected static RepositoryService repositoryService;
    protected static RuntimeService runtimeService;
    protected static TaskService taskService;
    protected static FormService formService;
    protected static HistoryService historyService;
    protected static IdentityService identityService;
    protected static ManagementService managementService;

    protected static MessageRepository messageRepository;

    protected ISO8601DateFormat dateFormat = new ISO8601DateFormat();

    static {
        createAndStartServer();

        // Lookup services
        processEngine = appContext.getBean("processEngine", ProcessEngine.class);
        processEngineConfiguration = appContext.getBean(ProcessEngineConfigurationImpl.class);
        repositoryService = appContext.getBean(RepositoryService.class);
        runtimeService = appContext.getBean(RuntimeService.class);
        taskService = appContext.getBean(TaskService.class);
        formService = appContext.getBean(FormService.class);
        historyService = appContext.getBean(HistoryService.class);
        identityService = appContext.getBean(IdentityService.class);
        managementService = appContext.getBean(ManagementService.class);

        messageRepository = appContext.getBean(MessageRepository.class);

        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                if (server != null && server.isRunning()) {
                    try {
                        server.stop();
                    } catch (Exception e) {
                        log.error("Error stopping server", e);
                    }
                }
            }
        });
    }

    @Override
    public void runBare() throws Throwable {
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
            processEngineConfiguration.getClock().reset();
        }
    }

    protected void createUsers() {
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

    public static void createAndStartServer() {
        server = new Server(HTTP_SERVER_PORT);

        AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();
        applicationContext.register(JPAApplicationConfiguration.class);
        applicationContext.refresh();

        appContext = applicationContext;

        try {
            server.setHandler(getServletContextHandler(applicationContext));
            server.start();
        } catch (Exception e) {
            log.error("Error starting server", e);
        }
    }

    private static ServletContextHandler getServletContextHandler(AnnotationConfigWebApplicationContext context) throws IOException {
        ServletContextHandler contextHandler = new ServletContextHandler();
        JPAWebConfigurer configurer = new JPAWebConfigurer();
        configurer.setContext(context);
        contextHandler.addEventListener(configurer);

        return contextHandler;
    }

    public HttpResponse executeHttpRequest(HttpUriRequest request, int expectedStatusCode) {
        CredentialsProvider provider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("kermit", "kermit");
        provider.setCredentials(AuthScope.ANY, credentials);
        HttpClient client = HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build();
        try {
            if (request.getFirstHeader(HttpHeaders.CONTENT_TYPE) == null) {
                // Revert to default content-type
                request.addHeader(new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json"));
            }
            HttpResponse response = client.execute(request);
            Assert.assertNotNull(response.getStatusLine());
            Assert.assertEquals(expectedStatusCode, response.getStatusLine().getStatusCode());
            return response;
        } catch (ClientProtocolException e) {
            Assert.fail(e.getMessage());
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
        return null;
    }

    public HttpResponse executeBinaryHttpRequest(HttpUriRequest request, int expectedStatusCode) {
        CredentialsProvider provider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("kermit", "kermit");
        provider.setCredentials(AuthScope.ANY, credentials);
        HttpClient client = HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build();
        try {
            HttpResponse response = client.execute(request);
            Assert.assertNotNull(response.getStatusLine());
            Assert.assertEquals(expectedStatusCode, response.getStatusLine().getStatusCode());
            return response;
        } catch (ClientProtocolException e) {
            Assert.fail(e.getMessage());
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
        return null;
    }

    protected void dropUsers() {
        IdentityService identityService = processEngine.getIdentityService();

        identityService.deleteUser("kermit");
        identityService.deleteGroup("admin");
        identityService.deleteMembership("kermit", "admin");
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
                if (count != 0L) {
                    outputMessage.append("  " + tableName + ": " + count + " record(s) ");
                }
            }
        }
        if (outputMessage.length() > 0) {
            outputMessage.insert(0, "DB NOT CLEAN: \n");
            log.error(EMPTY_LINE);
            log.error(outputMessage.toString());

            log.info("dropping and recreating db");

            CommandExecutor commandExecutor = ((ProcessEngineImpl) processEngine).getProcessEngineConfiguration().getCommandExecutor();
            commandExecutor.execute(new Command<Object>() {
                public Object execute(CommandContext commandContext) {
                    DbSqlSession session = commandContext.getSession(DbSqlSession.class);
                    session.dbSchemaDrop();
                    session.dbSchemaCreate();
                    return null;
                }
            });

            if (exception != null) {
                throw exception;
            } else {
                Assert.fail(outputMessage.toString());
            }
        } else {
            log.info("database was clean");
        }
    }

    protected String encode(String string) {
        if (string != null) {
            try {
                return URLEncoder.encode(string, "UTF-8");
            } catch (UnsupportedEncodingException uee) {
                throw new IllegalStateException("JVM does not support UTF-8 encoding.", uee);
            }
        }
        return null;
    }

    public void assertProcessEnded(final String processInstanceId) {
        ProcessInstance processInstance = processEngine
                .getRuntimeService()
                .createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();

        if (processInstance != null) {
            throw new AssertionFailedError("Expected finished process instance '" + processInstanceId + "' but it was still in the db");
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
                throw new ActivitiException("Exception while waiting on condition: " + e.getMessage(), e);
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
        HttpResponse response = executeHttpRequest(new HttpGet(SERVER_URL_PREFIX + url), HttpStatus.SC_OK);

        // Check status and size
        JsonNode dataNode = objectMapper.readTree(response.getEntity().getContent()).get("data");
        assertEquals(numberOfResultsExpected, dataNode.size());

        // Check presence of ID's
        List<String> toBeFound = new ArrayList<String>(Arrays.asList(expectedResourceIds));
        Iterator<JsonNode> it = dataNode.iterator();
        while (it.hasNext()) {
            String id = it.next().get("id").textValue();
            toBeFound.remove(id);
        }
        assertTrue("Not all process-definitions have been found in result, missing: " + StringUtils.join(toBeFound, ", "), toBeFound.isEmpty());
    }

    /**
     * Checks if the returned "data" array (child-node of root-json node returned by invoking a POST on the given url)
     * contains entries with the given ID's.
     */
    protected void assertResultsPresentInPostDataResponse(String url, ObjectNode body, String... expectedResourceIds) throws JsonProcessingException, IOException {
        assertResultsPresentInPostDataResponseWithStatusCheck(url, body, HttpStatus.SC_OK, expectedResourceIds);
    }

    protected void assertResultsPresentInPostDataResponseWithStatusCheck(String url, ObjectNode body, int expectedStatusCode, String... expectedResourceIds) throws JsonProcessingException, IOException {
        int numberOfResultsExpected = 0;
        if (expectedResourceIds != null) {
            numberOfResultsExpected = expectedResourceIds.length;
        }

        // Do the actual call
        HttpPost post = new HttpPost(SERVER_URL_PREFIX + url);
        post.setEntity(new StringEntity(body.toString()));
        HttpResponse response = executeHttpRequest(post, expectedStatusCode);

        if (expectedStatusCode == HttpStatus.SC_OK) {
            // Check status and size
            JsonNode rootNode = objectMapper.readTree(response.getEntity().getContent());
            JsonNode dataNode = rootNode.get("data");
            assertEquals(numberOfResultsExpected, dataNode.size());

            // Check presence of ID's
            if (expectedResourceIds != null) {
                List<String> toBeFound = new ArrayList<String>(Arrays.asList(expectedResourceIds));
                Iterator<JsonNode> it = dataNode.iterator();
                while (it.hasNext()) {
                    String id = it.next().get("id").textValue();
                    toBeFound.remove(id);
                }
                assertTrue("Not all entries have been found in result, missing: " + StringUtils.join(toBeFound, ", "), toBeFound.isEmpty());
            }
        }
    }

    /**
     * Checks if the rest operation returns an error as expected
     */
    protected void assertErrorResult(String url, ObjectNode body, int statusCode) throws IOException {

        // Do the actual call
        HttpPost post = new HttpPost(SERVER_URL_PREFIX + url);
        post.setEntity(new StringEntity(body.toString()));
        executeHttpRequest(post, statusCode);
    }

    /**
     * Extract a date from the given string. Assertion fails when invalid date has been provided.
     */
    protected Date getDateFromISOString(String isoString) {
        DateTimeFormatter dateFormat = ISODateTimeFormat.dateTime();
        try {
            return dateFormat.parseDateTime(isoString).toDate();
        } catch (IllegalArgumentException iae) {
            fail("Illegal date provided: " + isoString);
            return null;
        }
    }

    protected String getISODateString(Date time) {
        return dateFormat.format(time);
    }
}
