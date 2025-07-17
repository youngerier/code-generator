package com.example.demo;

import com.example.demo.codegen.config.CodeGenConfig;
import com.example.demo.codegen.config.CodeGenConfigLoader;
import com.example.demo.model.dal.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * 代码生成器测试
 */
@Slf4j
public class CodeGenTest {

    @Test
    public void testDefaultConfig() {
        log.info("=== 测试默认配置 ===");
        CodeGenConfig config = CodeGenConfig.builder()
                .basePackage("com.example.demo.model")
                .build();

        log.info("基础包名: {}", config.getBasePackage());
        log.info("输出目录: {}", config.getOutputDir());
        log.info("DTO包名: {}", config.getFullPackage("dto"));
        log.info("Mapper包名: {}", config.getFullPackage("mapper"));
        log.info("Service包名: {}", config.getFullPackage("service"));
    }

    @Test
    public void testCustomConfig() {
        log.info("=== 测试自定义配置 ===");

        Map<String, String> packageSuffixes = new HashMap<>();
        packageSuffixes.put("dto", ".model.dto");
        packageSuffixes.put("mapper", ".dal.mapper");
        packageSuffixes.put("service", ".business.service");

        Map<String, Boolean> templateEnabled = new HashMap<>();
        templateEnabled.put("dto", true);
        templateEnabled.put("mapper", true);
        templateEnabled.put("service", false);

        CodeGenConfig config = CodeGenConfig.builder()
                .outputDir(Paths.get("target/generated-sources"))
                .basePackage("com.custom.project")
                .entityPackageSuffix(".domain.entity")
                .packageSuffixes(packageSuffixes)
                .templateEnabled(templateEnabled)
                .build();

        log.info("基础包名: {}", config.getBasePackage());
        log.info("输出目录: {}", config.getOutputDir());
        log.info("实体包后缀: {}", config.getEntityPackageSuffix());
        log.info("DTO包名: {}", config.getFullPackage("dto"));
        log.info("Mapper包名: {}", config.getFullPackage("mapper"));
        log.info("Service包名: {}", config.getFullPackage("service"));
        log.info("DTO模板启用: {}", config.isTemplateEnabled("dto"));
        log.info("Service模板启用: {}", config.isTemplateEnabled("service"));
    }

    @Test
    public void testConfigLoader() {
        log.info("=== 测试配置加载器 ===");

        CodeGenConfig config = CodeGenConfigLoader.loadFromClasspath();

        log.info("基础包名: {}", config.getBasePackage());
        log.info("输出目录: {}", config.getOutputDir());
        log.info("实体包后缀: {}", config.getEntityPackageSuffix());
        log.info("DTO包名: {}", config.getFullPackage("dto"));
        log.info("Mapper包名: {}", config.getFullPackage("mapper"));
        log.info("Service包名: {}", config.getFullPackage("service"));

        // 测试模板启用状态
        log.info("DTO模板启用: {}", config.isTemplateEnabled("dto"));
        log.info("Mapper模板启用: {}", config.isTemplateEnabled("mapper"));
        log.info("Service模板启用: {}", config.isTemplateEnabled("service"));
    }

    @Test
    public void testCodeGeneratorCreation() {
        log.info("=== 测试代码生成器创建 ===");

        // 测试默认构造器
        CodeGenerator defaultGenerator = new CodeGenerator();
        log.info("默认生成器配置: {}", defaultGenerator.getConfig().getBasePackage());

        // 测试从配置文件创建
        CodeGenerator configFileGenerator = CodeGenerator.fromConfigFile();
        log.info("配置文件生成器配置: {}", configFileGenerator.getConfig().getBasePackage());

        // 测试自定义配置
        CodeGenConfig customConfig = CodeGenConfig.builder()
                .basePackage("com.test.project")
                .build();
        CodeGenerator customGenerator = new CodeGenerator(customConfig);
        log.info("自定义生成器配置: {}", customGenerator.getConfig().getBasePackage());
    }

    @Test
    public void testCodeGen() {
        // 示例1: 使用默认配置
        log.info("=== 使用默认配置生成代码 ===");
        new CodeGenerator().generate(User.class);
    }
}