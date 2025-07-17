package com.example.demo.codegen.core;

import com.github.javaparser.ast.body.FieldDeclaration;
import lombok.Data;
import lombok.Builder;

import java.util.List;

/**
 * 实体元数据
 */
@Data
@Builder
public class EntityMetadata {
    
    /**
     * 类名
     */
    private String className;
    
    /**
     * 包名
     */
    private String packageName;
    
    /**
     * 基础包名（去除entity后缀）
     */
    private String basePackage;
    
    /**
     * 类注释
     */
    private String classJavadoc;
    
    /**
     * ID字段类型
     */
    private String idType;
    
    /**
     * 所有字段
     */
    private List<FieldDeclaration> fields;
    
    /**
     * 实体类
     */
    private Class<?> entityClass;
}