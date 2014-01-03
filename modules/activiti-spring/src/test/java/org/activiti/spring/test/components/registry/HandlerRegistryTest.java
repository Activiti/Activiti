package org.activiti.spring.test.components.registry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class HandlerRegistryTest {

    @Configuration
    public static class ActivitiConfiguration {


    }

    @Before
    public void before() {
    }

    @After
    public void after() {
    }

    @Test
    public void testRegisteringComponents() {

    }
}


