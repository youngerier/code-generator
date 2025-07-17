package com.example.demo.codegen.template;

import com.example.demo.codegen.config.CodeGenConfig;
import com.example.demo.codegen.core.EntityMetadata;
import com.example.demo.template.TemplateUtils;
import com.mybatisflex.core.BaseMapper;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import java.io.IOException;

/**
 * MyBatis-Flex Mapper代码模板
 */
public class MapperCodeTemplate implements CodeTemplate {
    
    @Override
    public void generate(EntityMetadata metadata, CodeGenConfig config) throws IOException {
        TypeName entityType = ClassName.get(metadata.getPackageName(), metadata.getClassName());
        TypeName idTypeName = TemplateUtils.resolveTypeName(metadata.getIdType());
        
        TypeSpec mapper = TypeSpec.interfaceBuilder(metadata.getClassName() + "FlexMapper")
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ParameterizedTypeName.get(
                        ClassName.get(BaseMapper.class),
                        entityType))
                .addJavadoc(metadata.getClassJavadoc() + "\nMyBatis-Flex Mapper interface for " + metadata.getClassName() + "\n")
                .build();
        
        JavaFile javaFile = JavaFile.builder(config.getFullPackage("mapper"), mapper)
                .build();
        
        javaFile.writeTo(config.getOutputDir());
    }
    
    @Override
    public String getTemplateName() {
        return "mapper";
    }
}