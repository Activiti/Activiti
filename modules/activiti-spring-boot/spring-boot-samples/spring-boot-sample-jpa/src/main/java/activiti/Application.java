package activiti;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.*;

@SpringBootApplication
public class Application {

    @Bean
    CommandLineRunner init(final PhotoService photoService) {
        return new CommandLineRunner() {
            @Override
            public void run(String... strings) throws Exception {
                photoService.launchPhotoProcess("one", "two", "three");
            }
        };

    }

    public static void main(String args[]) {
        SpringApplication.run(Application.class, args);
    }
}

@Service
@Transactional
class PhotoService {

    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final PhotoRepository photoRepository;

    @Autowired
    public PhotoService(RuntimeService runtimeService, TaskService taskService, PhotoRepository photoRepository) {
        this.runtimeService = runtimeService;
        this.taskService = taskService;
        this.photoRepository = photoRepository;
    }

    public void processPhoto(Long photoId) {
        System.out.println("processing photo#" + photoId);
    }

    public void launchPhotoProcess(String... photoLabels) {
        List<Photo> photos = new ArrayList<Photo>();
        for (String l : photoLabels) {
            Photo x = this.photoRepository.save(new Photo(l));
            photos.add(x);
        }

        Map<String, Object> procVars = new HashMap<String, Object>();
        procVars.put("photos", photos);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("dogeProcess", procVars);

        List<Execution> waitingExecutions = runtimeService.createExecutionQuery().activityId("wait").list();
        System.out.println("--> # executions = " + waitingExecutions.size());

        for (Execution execution : waitingExecutions) {
            runtimeService.signal(execution.getId());
        }

        Task reviewTask = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        taskService.complete(reviewTask.getId(), Collections.singletonMap("approved", (Object) true));

        long count = runtimeService.createProcessInstanceQuery().count();
        System.out.println("Proc count " + count);

    }
}

interface PhotoRepository extends JpaRepository<Photo, Long> {
}

@Entity
class Photo {

    @Id
    @GeneratedValue
    private Long id;

    Photo() {
    }

    Photo(String username) {
        this.username = username;
    }

    private String username;

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }
}