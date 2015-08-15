package org.activiti5.engine.test.db;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.test.Deployment;
import org.activiti5.engine.impl.test.ResourceActivitiTestCase;


public class IdGeneratorDataSourceTest extends ResourceActivitiTestCase {

  public IdGeneratorDataSourceTest() {
    super("org/activiti5/engine/test/db/IdGeneratorDataSourceTest.activiti.cfg.xml");
  }

  @Deployment
  public void testIdGeneratorDataSource() {
    List<Thread> threads = new ArrayList<Thread>();
    for (int i=0; i<20; i++) {
      Thread thread = new Thread() {
        public void run() {
          for (int j = 0; j < 5; j++) {
            runtimeService.startProcessInstanceByKey("idGeneratorDataSource");
          }
        }
      };
      thread.start();
      threads.add(thread);
    }
    
    for (Thread thread: threads) {
      try {
        thread.join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}
