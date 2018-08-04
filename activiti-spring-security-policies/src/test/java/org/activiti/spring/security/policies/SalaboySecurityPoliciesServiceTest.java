package org.activiti.spring.security.policies;

import org.activiti.spring.security.policies.conf.SecurityPoliciesProperties;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource("classpath:propstest.properties")
public class SalaboySecurityPoliciesServiceTest {

    @Autowired
    private SecurityPoliciesProperties securityPoliciesProperties;



}
