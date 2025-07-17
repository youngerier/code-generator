package com.example.demo;

import com.example.demo.codegen.config.CodeGenConfig;
import com.example.demo.codegen.config.CodeGenConfigLoader;
import com.example.demo.codegen.core.EntityMetadata;
import com.example.demo.codegen.core.EntityParser;
import com.example.demo.codegen.template.CodeTemplate;
import com.example.demo.codegen.template.DtoCodeTemplate;
import com.example.demo.codegen.template.MapperCodeTemplate;
import com.example.demo.codegen.template.ServiceCodeTemplate;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 重构后的代码生成器
 * 架构简洁清晰，支持配置化，去除硬编码
 */
@Slf4j
public class CodeGenerator {
    
    private final CodeGenConfig config;
    private final EntityParser parser;
    private final List<CodeTemplate> templates;
    
    public CodeGenerator(CodeGenConfig config) {
        this.config = config;
        this.parser = new EntityParser(config);
        this.templates = initializeTemplates();
    }
    
    /**
     * 使用默认配置的构造器
     */
    public CodeGenerator() {
        this(CodeGenConfig.builder()
                .basePackage("com.example.demo.model")
                .build());
    }
    
    /**
     * 从配置文件创建代码生成器
     */
    public static CodeGenerator fromConfigFile() {
        return new CodeGenerator(CodeGenConfigLoader.loadFromClasspath());
    }
    
    /**
     * 从指定配置文件创建代码生成器
     */
    public static CodeGenerator fromConfigFile(String configFile) {
        return new CodeGenerator(CodeGenConfigLoader.loadFromClasspath(configFile));
    }
    
    /**
     * 生成代码
     */
    public void generate(Class<?> entityClass) {
        try {
            // 解析实体元数据
            EntityMetadata metadata = parser.parse(entityClass);
            
            // 更新配置中的基础包名
            config.setBasePackage(metadata.getBasePackage());
            
            // 执行所有启用的模板
            for (CodeTemplate template : templates) {
                if (config.isTemplateEnabled(template.getTemplateName())) {
                    try {
                        template.generate(metadata, config);
                        log.debug("Generated {} for {}", template.getTemplateName(), entityClass.getSimpleName());
                    } catch (Exception e) {
                        log.error("Failed to generate {} for {}: {}", 
                                template.getTemplateName(), entityClass.getSimpleName(), e.getMessage());
                    }
                }
            }
            
            log.info("Code generation completed for {}", entityClass.getName());
            
        } catch (IOException e) {
            log.error("Error during code generation for {}: {}", entityClass.getName(), e.getMessage());
            throw new RuntimeException("Code generation failed", e);
        }
    }
    
    /**
     * 静态方法保持向后兼容
     */
    public static void generate(Class<?> entityClass) {
        new CodeGenerator().generate(entityClass);
    }
    
    /**
     * 初始化模板列表
     */
    private List<CodeTemplate> initializeTemplates() {
        List<CodeTemplate> templateList = new ArrayList<>();
        templateList.add(new DtoCodeTemplate());
        templateList.add(new MapperCodeTemplate());
        templateList.add(new ServiceCodeTemplate());
        // 可以继续添加其他模板
        return templateList;
    }
    
    /**
     * 添加自定义模板
     */
    public void addTemplate(CodeTemplate template) {
        templates.add(template);
    }
    
    /**
     * 获取配置
     */
    public CodeGenConfig getConfig() {
        return config;
    }
}