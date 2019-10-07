package org.activiti.core.common.spring.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.api.runtime.shared.security.SecurityManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class LocalSpringSecurityManagerTest {

    @MockBean
    private UserDetailsService userDetailsService;
    
    @Autowired
    private SecurityManager securityManager;
    
    @Test
    public void contextLoads() {
        assertThat(securityManager).isInstanceOf(LocalSpringSecurityManager.class);
    }
    
    @SpringBootApplication
    static class Application {
        
    }

}
