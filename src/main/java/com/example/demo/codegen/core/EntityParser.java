package com.example.demo.codegen.core;

import com.example.demo.codegen.config.CodeGenConfig;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;

/**
 * 实体解析器
 */
@Slf4j
@RequiredArgsConstructor
public class EntityParser {
    
    private final CodeGenConfig config;
    
    /**
     * 解析实体类
     */
    public EntityMetadata parse(Class<?> entityClass) throws IOException {
        // 获取源码文件路径
        String classPath = entityClass.getName().replace(".", "/") + ".java";
        String sourcePath = config.getOutputDir() + "/" + classPath;
        File sourceFile = new File(sourcePath);
        
        if (!sourceFile.exists()) {
            throw new IllegalArgumentException("Source file not found: " + sourcePath);
        }
        
        // 解析源码
        CompilationUnit cu = StaticJavaParser.parse(sourceFile);
        String className = entityClass.getSimpleName();
        
        ClassOrInterfaceDeclaration parsedClass = cu.getClassByName(className)
                .orElseThrow(() -> new IllegalArgumentException(className + " class not found"));
        
        // 获取基础包名
        String basePackage = extractBasePackage(entityClass.getPackageName());
        
        // 获取类注释
        String classJavadoc = parsedClass.getJavadocComment()
                .map(JavadocComment::getContent)
                .orElse("Generated class for " + className);
        
        // 获取ID字段类型
        String idType = TemplateUtils.getIdFieldType(parsedClass);
        
        return EntityMetadata.builder()
                .className(className)
                .packageName(entityClass.getPackageName())
                .basePackage(basePackage)
                .classJavadoc(classJavadoc)
                .idType(idType)
                .fields(parsedClass.getFields())
                .entityClass(entityClass)
                .build();
    }
    
    /**
     * 提取基础包名
     */
    private String extractBasePackage(String fullPackage) {
        int entityIndex = fullPackage.lastIndexOf(config.getEntityPackageSuffix());
        if (entityIndex > 0) {
            return fullPackage.substring(0, entityIndex);
        }
        return fullPackage;
    }
}