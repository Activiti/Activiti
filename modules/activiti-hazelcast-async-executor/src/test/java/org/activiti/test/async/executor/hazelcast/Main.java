package org.activiti.test.async.executor.hazelcast;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;

/**
 * @author Joram Barrez
 */
public class Main {
	
	private static ProcessEngine processEngine;

	public static void main(String[] args) throws Exception {
		
		if (args.length == 0) {
			System.err.println("Arguments needed");
			System.err.println("First argument: { job-creator | job-executor | mixed}, to determine what this Main program will be doing: creating jobs, executing them or both");
			System.err.println("Arguments in case of job-creator: { nrOfJobs nrOfJobCreationThreads}");
			System.err.println("Arguments in case of job-executor: { nrOfJobs }");
			System.err.println("In case of mixed: { nrOfJobs nrOfJobCreationThreads } ");
			System.err.println();
			System.err.println("Example usage : java -XX:MaxPermSize=512m -Xmx2G -Xms512m -jar theJar.jar job-creator 1000 4");
			System.err.println("Example usage : java -XX:MaxPermSize=512m -Xmx2G -Xms512m -jar theJar.jar job-executor 1000");
			System.err.println("Example usage : java -XX:MaxPermSize=512m -Xmx2G -Xms512m -jar theJar.jar mixed 1000 4");
			System.err.println();
			System.err.println("The last argument can always be 'keepDb'. In that case, no drop of the database schema will be done, which is the default for job creation or mixed.");
			System.exit(-1);
		}
		
		int nrOfJobs = Integer.valueOf(args[1]);
		int nrOfThreads = args[2] != null ? Integer.valueOf(args[2]) : -1;
		boolean keepDb = "keepDb".equals(args[args.length - 1]); 
		boolean dropDb = !keepDb;
		
		if ("job-creator".equals(args[0])) {
			startCreateJobs(nrOfJobs, nrOfThreads, dropDb);
		} else if ("job-executor".equals(args[0])) {
			startExecuteJobs(nrOfJobs, dropDb);
		} else if ("mixed".equals(args[0])) {
			startMixed(nrOfJobs, nrOfThreads, dropDb);
		} else {
			System.err.println("Unknown argument '" + args[0] + "'");
			System.exit(-1);
		}
		
	}
	
	private static void startCreateJobs(int nrOfJobs, int nrOfThreads, boolean dropDb) throws Exception {
		
		System.out.println(new Date() +  " [Job Creator] Starting. Need to create " + nrOfJobs + " job with " + nrOfThreads + " threads");
		
		// Job creator needs to be started first, as it will clean the database!
		
		createJobCreatorProcessEngine(false, dropDb);
		processEngine.getRepositoryService().createDeployment().name("job")
		    .addClasspathResource("job.bpmn20.xml").deploy();

		ExecutorService executor = Executors.newFixedThreadPool(nrOfThreads);
		for (int i = 0; i < nrOfJobs; i++) {
			Runnable worker = new StartTestProcessInstanceThread();
			executor.execute(worker);
		}
		executor.shutdown();
		System.out.println(new Date() + " [Job Creator] All StartProcessInstanceThreads handed off to executor service");
		
		boolean finishedAllInstances = false;
		long unfinishedCount = 0;
		long finishedCount = 0;
		while (finishedAllInstances == false) {
			unfinishedCount = processEngine.getHistoryService()
			    .createHistoricProcessInstanceQuery().unfinished().count();
			finishedCount = processEngine.getHistoryService()
			    .createHistoricProcessInstanceQuery().finished().count();
			
			finishedAllInstances = finishedCount >= nrOfJobs;
			
			if (!finishedAllInstances) {
				System.out.println(new Date() + " [Job Creator] " + finishedCount + " finished process instances in db, " + unfinishedCount + " unfinished process instances in db.");
				Thread.sleep(10000L);
			}
		}
	}

	private static void startExecuteJobs(int nrOfJobs, boolean dropDb) throws Exception {
		
		System.out.println(new Date() +  " [Job Executor] Starting. Need to execute " + nrOfJobs + " jobs in total.");
		
		createJobExecutorProcessEngine(false, dropDb);
		
		boolean finishedAllInstances = false;
		long lastPrintTime = 0;
		long finishedCount = 0;
		long start = System.currentTimeMillis();
		while (finishedAllInstances == false) {
			finishedCount = processEngine.getHistoryService()
			    .createHistoricProcessInstanceQuery().finished().count();
			if (finishedCount >= nrOfJobs) {
				finishedAllInstances = true;
			} else {
				if (System.currentTimeMillis() - lastPrintTime > 5000L) {
					lastPrintTime = System.currentTimeMillis();
					int percentage = (int) (((double) finishedCount / (double) nrOfJobs) * 100.0);
					System.out.println(new Date() +  " [Job Executor] Executed " + finishedCount + "/" + nrOfJobs + " jobs (" + percentage + "%)");
				}
				Thread.sleep(1000L);
			}
		}

		long end = System.currentTimeMillis();

		System.out.println();
		System.out.println();
		System.out.println("Jobs executed by this node: " + SleepDelegate.nrOfExecutions.get());
		
		long time = end - start;
		System.out.println("Took " + time + " ms");
		double perSecond = ((double) SleepDelegate.nrOfExecutions.get() / (double) time) * 1000;
		System.out.println("Which is " + perSecond + " jobs/second");
		System.out.println();
		System.out.println();
	}

	private static void startMixed(final int nrOfJobs, final int nrOfThreads, boolean dropDb) throws Exception {
		createJobExecutorProcessEngine(true, dropDb);
		
		// Create Jobs
		Thread createJobsThread = new Thread(new Runnable() {
			public void run() {
				try {
	        startCreateJobs(nrOfJobs, nrOfThreads, false);
        } catch (Exception e) {
	        e.printStackTrace();
	        System.exit(-1);
        }
			}
		});
		createJobsThread.start();
		
		// Execute jobs
		Thread executeJobsThread = new Thread(new Runnable() {
			
			public void run() {
				try {
	        startExecuteJobs(nrOfJobs, false);
        } catch (Exception e) {
	        e.printStackTrace();
	        System.exit(-1);
        }
			}
		});
		executeJobsThread.start();
		
		createJobsThread.join();
		executeJobsThread.join();
		
		processEngine.close();
		System.out.println("Done.");
		System.exit(0);
	}
	
	private static void createJobCreatorProcessEngine(boolean replaceExisting, boolean isDropDatabaseSchema) {
		if (processEngine == null || replaceExisting) {
			
			System.out.println("Creating process engine with config activiti_job_creator.cfg.xml. Dropping db first = " + isDropDatabaseSchema);
			
			ProcessEngineConfiguration processEngineConfiguration = ProcessEngineConfiguration
			    .createProcessEngineConfigurationFromResource("activiti_job_creator.cfg.xml");
			    
			 if (isDropDatabaseSchema) {
				 processEngineConfiguration.setDatabaseSchemaUpdate("drop-create");
			 }
		   processEngine = processEngineConfiguration.buildProcessEngine();
		} 
  }
	
	private static void createJobExecutorProcessEngine(boolean replaceExisting, boolean isDropDatabaseSchema) {
		if (processEngine == null || replaceExisting) {
			
			System.out.println("Creating process engine with config activiti_with_jobexecutor.cfg.xml. Dropping db first = " + isDropDatabaseSchema);
			
			 ProcessEngineConfiguration processEngineConfiguration = ProcessEngineConfiguration
					 .createProcessEngineConfigurationFromResource("activiti_with_jobexecutor.cfg.xml");
			 if (isDropDatabaseSchema) {
				 processEngineConfiguration.setDatabaseSchemaUpdate("drop-create");
			 }
		   processEngine = processEngineConfiguration.buildProcessEngine();
		}
  }

	public static class StartTestProcessInstanceThread implements Runnable {
		
		public void run() {
			processEngine.getRuntimeService().startProcessInstanceByKey("job");
		}

	}

}
