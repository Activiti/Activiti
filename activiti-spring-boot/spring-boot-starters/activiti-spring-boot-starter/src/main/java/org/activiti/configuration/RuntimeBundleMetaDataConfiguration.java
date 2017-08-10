package org.activiti.configuration;

import java.util.HashMap;
import java.util.Map;

import com.netflix.appinfo.ApplicationInfoManager;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RuntimeBundleMetaDataConfiguration implements BeanClassLoaderAware {

    private ClassLoader classLoader;


    private ApplicationInfoManager appInfoManager;

    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Autowired
    public RuntimeBundleMetaDataConfiguration(ApplicationInfoManager appInfoManager) {
        this.appInfoManager = appInfoManager;
        Map<String, String> metadata = new HashMap<>();
        metadata.put("type", "runtime-bundle");
        this.appInfoManager.registerAppMetadata(metadata);
    }
}
