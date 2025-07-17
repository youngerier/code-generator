package com.example.demo;

import com.example.demo.codegen.config.CodeGenConfig;
import com.example.demo.model.dal.entity.User;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * 代码生成器使用示例
 */
@Slf4j
public class CodeGeneratorExample {
    
    public static void main(String[] args) {
        // 示例1: 使用默认配置
        log.info("=== 使用默认配置生成代码 ===");
        CodeGenerator.generate(User.class);
        
        // 示例2: 使用自定义配置
        log.info("=== 使用自定义配置生成代码 ===");
        CodeGenConfig customConfig = createCustomConfig();
        CodeGenerator generator = new CodeGenerator(customConfig);
        generator.generate(User.class);
        
        // 示例3: 只生成特定模板
        log.info("=== 只生成DTO和Service ===");
        CodeGenConfig selectiveConfig = createSelectiveConfig();
        CodeGenerator selectiveGenerator = new CodeGenerator(selectiveConfig);
        selectiveGenerator.generate(User.class);
    }
    
    /**
     * 创建自定义配置
     */
    private static CodeGenConfig createCustomConfig() {
        Map<String, String> packageSuffixes = new HashMap<>();
        packageSuffixes.put("dto", ".model.dto");
        packageSuffixes.put("mapper", ".model.mapper");
        packageSuffixes.put("service", ".business.service");
        packageSuffixes.put("serviceImpl", ".business.service.impl");
        packageSuffixes.put("query", ".model.query");
        
        return CodeGenConfig.builder()
                .outputDir(Paths.get("src/main/java"))
                .basePackage("com.example.demo")
                .entityPackageSuffix(".model.dal.entity")
                .packageSuffixes(packageSuffixes)
                .build();
    }
    
    /**
     * 创建选择性配置（只启用部分模板）
     */
    private static CodeGenConfig createSelectiveConfig() {
        Map<String, Boolean> templateEnabled = new HashMap<>();
        templateEnabled.put("dto", true);
        templateEnabled.put("service", true);
        templateEnabled.put("mapper", false);
        templateEnabled.put("serviceImpl", false);
        templateEnabled.put("mapstruct", false);
        templateEnabled.put("query", false);
        
        return CodeGenConfig.builder()
                .basePackage("com.example.demo.model")
                .templateEnabled(templateEnabled)
                .build();
    }
}