package com.example.demo.codegen.core;

import com.example.demo.codegen.config.CodeGenConfig;

import java.io.IOException;

/**
 * 代码模板接口
 */
public interface CodeTemplate {
    
    /**
     * 生成代码
     */
    void generate(EntityMetadata metadata, CodeGenConfig config) throws IOException;
    
    /**
     * 获取模板名称
     */
    String getTemplateName();
}
