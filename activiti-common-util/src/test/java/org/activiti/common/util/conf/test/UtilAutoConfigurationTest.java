package org.activiti.common.util.conf.test;

import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.common.util.DateFormatterProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UtilAutoConfigurationTest {
    
    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class Configuration {};
    
    @Autowired
    private DateFormatterProvider dateFormatterProvider;
    
    @Test
    public void contextLoad() {
        assertThat(dateFormatterProvider).isNotNull();
    }
}
