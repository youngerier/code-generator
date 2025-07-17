package com.example.demo.codegen.config;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 代码生成器配置加载器
 */
@Slf4j
public class CodeGenConfigLoader {
    
    private static final String DEFAULT_CONFIG_FILE = "codegen.properties";
    
    /**
     * 从默认配置文件加载配置
     */
    public static CodeGenConfig loadFromClasspath() {
        return loadFromClasspath(DEFAULT_CONFIG_FILE);
    }
    
    /**
     * 从指定配置文件加载配置
     */
    public static CodeGenConfig loadFromClasspath(String configFile) {
        try (InputStream inputStream = CodeGenConfigLoader.class.getClassLoader()
                .getResourceAsStream(configFile)) {
            
            if (inputStream == null) {
                log.warn("配置文件 {} 不存在，使用默认配置", configFile);
                return createDefaultConfig();
            }
            
            Properties properties = new Properties();
            properties.load(inputStream);
            
            return buildConfigFromProperties(properties);
            
        } catch (IOException e) {
            log.error("加载配置文件失败: {}", e.getMessage());
            return createDefaultConfig();
        }
    }
    
    /**
     * 从Properties构建配置
     */
    private static CodeGenConfig buildConfigFromProperties(Properties properties) {
        CodeGenConfig.CodeGenConfigBuilder builder = CodeGenConfig.builder();
        
        // 基础配置
        String outputDir = properties.getProperty("codegen.output.dir", "src/main/java");
        builder.outputDir(Paths.get(outputDir));
        
        String basePackage = properties.getProperty("codegen.base.package", "com.example.demo.model");
        builder.basePackage(basePackage);
        
        String entityPackageSuffix = properties.getProperty("codegen.entity.package.suffix", ".dal.entity");
        builder.entityPackageSuffix(entityPackageSuffix);
        
        // 包名配置
        Map<String, String> packageSuffixes = new HashMap<>();
        packageSuffixes.put("dto", properties.getProperty("codegen.package.dto", ".dto"));
        packageSuffixes.put("mapper", properties.getProperty("codegen.package.mapper", ".mapper.flex"));
        packageSuffixes.put("service", properties.getProperty("codegen.package.service", ".service"));
        packageSuffixes.put("serviceImpl", properties.getProperty("codegen.package.serviceImpl", ".service.impl"));
        packageSuffixes.put("query", properties.getProperty("codegen.package.query", ".query"));
        builder.packageSuffixes(packageSuffixes);
        
        // 模板启用配置
        Map<String, Boolean> templateEnabled = new HashMap<>();
        templateEnabled.put("dto", getBooleanProperty(properties, "codegen.template.dto.enabled", true));
        templateEnabled.put("mapper", getBooleanProperty(properties, "codegen.template.mapper.enabled", true));
        templateEnabled.put("service", getBooleanProperty(properties, "codegen.template.service.enabled", true));
        templateEnabled.put("serviceImpl", getBooleanProperty(properties, "codegen.template.serviceImpl.enabled", true));
        templateEnabled.put("mapstruct", getBooleanProperty(properties, "codegen.template.mapstruct.enabled", true));
        templateEnabled.put("query", getBooleanProperty(properties, "codegen.template.query.enabled", true));
        builder.templateEnabled(templateEnabled);
        
        return builder.build();
    }
    
    /**
     * 获取布尔属性值
     */
    private static boolean getBooleanProperty(Properties properties, String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }
    
    /**
     * 创建默认配置
     */
    private static CodeGenConfig createDefaultConfig() {
        return CodeGenConfig.builder()
                .basePackage("com.example.demo.model")
                .build();
    }
}