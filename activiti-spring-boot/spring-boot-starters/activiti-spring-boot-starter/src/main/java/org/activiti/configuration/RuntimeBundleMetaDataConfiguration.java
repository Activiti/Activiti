package org.activiti.configuration;

import java.util.HashMap;
import java.util.Map;

import com.netflix.appinfo.ApplicationInfoManager;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnBean(ApplicationInfoManager.class)
public class RuntimeBundleMetaDataConfiguration implements BeanClassLoaderAware {

    @Autowired
    public RuntimeBundleMetaDataConfiguration(ApplicationInfoManager appInfoManager) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("type",
                     "runtime-bundle");
        appInfoManager.registerAppMetadata(metadata);
    }

    public void setBeanClassLoader(ClassLoader classLoader) {
        // Do nothing for now
    }
}
