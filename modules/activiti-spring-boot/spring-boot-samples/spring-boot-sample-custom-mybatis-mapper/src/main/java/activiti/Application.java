package activiti;

import activiti.mappers.CustomMybatisMapper;
import org.activiti.engine.ManagementService;
import org.activiti.engine.impl.cmd.AbstractCustomSqlExecution;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

/**
 *  @author Dominik Bartos
 */
@SpringBootApplication
public class Application {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    @Bean
    CommandLineRunner customMybatisMapper(final ManagementService managementService) {
        return new CommandLineRunner() {
            @Override
            public void run(String... args) throws Exception {
                String processDefinitionId = managementService.executeCustomSql(new AbstractCustomSqlExecution<CustomMybatisMapper, String>(CustomMybatisMapper.class) {
                    @Override
                    public String execute(CustomMybatisMapper customMybatisMapper) {
                        return customMybatisMapper.loadProcessDefinitionIdByKey("waiter");
                    }
                });

                logger.info("Process definition id = {}", processDefinitionId);
            }
        };
    }

    @Bean
    CommandLineRunner customMybatisXmlMapper(final ManagementService managementService) {
        return new CommandLineRunner() {
            @Override
            public void run(String... args) throws Exception {
                String processDefinitionDeploymentId = managementService.executeCommand(new Command<String>() {
                    @Override
                    public String execute(CommandContext commandContext) {
                        return (String) commandContext
                                .getDbSqlSession()
                                .selectOne("selectProcessDefinitionDeploymentIdByKey", "waiter");
                    }
                });

                logger.info("Process definition deployment id = {}", processDefinitionDeploymentId);
            }
        };
    }


    public static void main(String args[]) {
        SpringApplication.run(Application.class, args);
    }

}
