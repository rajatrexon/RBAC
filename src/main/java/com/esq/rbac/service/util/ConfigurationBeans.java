package com.esq.rbac.service.util;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import javax.sql.DataSource;
import java.io.IOException;


@Configuration
public class ConfigurationBeans {


    @Autowired
    private DataSource dataSource;

    @Value("${rbac.configuration.tableName}")
    private String name;

    @Value("${rbac.configuration.keyColumn}")
    private String confKey;

    @Value("${rbac.configuration.valueColumn}")
    private String confValue;

    @Value("${rbac.configuration.cacheReloadTimeSec}")
    private int cacheExpirationTime;

    //   Assuming you have defined the dataSource bean in a separate configuration class


    @Bean
    public ReloadableResourceBundleMessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new
                ReloadableResourceBundleMessageSource();
        return messageSource;
    }


    @Lazy
    @Bean(name="DatabaseConfigurationWithCache")
    public DatabaseConfigurationWithCache databaseConfigurationWithCache() {
        return new DatabaseConfigurationWithCache(dataSource, name, confKey, confValue, cacheExpirationTime);
    }




    private final ResourceLoader resourceLoader;


    @Autowired
    public ConfigurationBeans(ResourceLoader resourceLoader){
        this.resourceLoader=resourceLoader;
    }



//    @Bean(name="propertyConfig")
//    public PropertiesConfiguration propertyConfig() throws ConfigurationException, IOException {
//
//        Resource resource = resourceLoader.getResource("classpath:runtime.properties");
//        URL url = resource.getURL();
//        String path = url.getPath();
//        PropertiesConfiguration config = new PropertiesConfiguration();
//        config.setURL(new URL(path));
//
//        FileChangedReloadingStrategy reloadingStrategy = new FileChangedReloadingStrategy();
//        config.setReloadingStrategy(reloadingStrategy);
//
//        return config;
//    }


    @Lazy
    @Bean(name = "propertyConfig")
    public PropertiesConfiguration propertyConfig() throws ConfigurationException, IOException {
        Resource resource = resourceLoader.getResource("classpath:runtime.properties");

        PropertiesConfiguration config = new PropertiesConfiguration();
        config.load(resource.getInputStream());

        FileChangedReloadingStrategy reloadingStrategy = new FileChangedReloadingStrategy();
        config.setReloadingStrategy(reloadingStrategy);

        return config;
    }
}
