package com.example.demo.codegen.config;

import lombok.Data;
import lombok.Builder;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.HashMap;

/**
 * 代码生成器配置类
 */
@Data
@Builder
public class CodeGenConfig {
    
    /**
     * 输出目录
     */
    @Builder.Default
    private Path outputDir = Paths.get("src/main/java");
    
    /**
     * 基础包名
     */
    private String basePackage;

    private String testPackage;
    
    /**
     * 实体包后缀
     */
    @Builder.Default
    private String entityPackageSuffix = ".dal.entity";
    
    /**
     * 各层包名配置
     */
    @Builder.Default
    private Map<String, String> packageSuffixes = new HashMap<String, String>() {{
        put("dto", ".dto");
        put("mapper", ".mapper.flex");
        put("service", ".service");
        put("serviceImpl", ".service.impl");
        put("query", ".query");
    }};
    
    /**
     * 模板配置
     */
    @Builder.Default
    private Map<String, Boolean> templateEnabled = new HashMap<String, Boolean>() {{
        put("dto", true);
        put("mapper", true);
        put("service", true);
        put("serviceImpl", true);
        put("mapstruct", true);
        put("query", true);
    }};
    
    /**
     * 获取完整包名
     */
    public String getFullPackage(String layer) {
        return basePackage + packageSuffixes.getOrDefault(layer, "");
    }
    
    /**
     * 检查模板是否启用
     */
    public boolean isTemplateEnabled(String templateName) {
        return templateEnabled.getOrDefault(templateName, false);
    }
}