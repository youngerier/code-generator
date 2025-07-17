package com.example.demo.codegen.template;

import com.example.demo.codegen.config.CodeGenConfig;
import com.example.demo.codegen.core.EntityMetadata;
import com.example.demo.template.TemplateUtils;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.squareup.javapoet.*;
import lombok.Data;

import javax.lang.model.element.Modifier;
import java.io.IOException;

/**
 * DTO代码模板
 */
public class DtoCodeTemplate implements CodeTemplate {
    
    @Override
    public void generate(EntityMetadata metadata, CodeGenConfig config) throws IOException {
        TypeSpec.Builder dtoBuilder = TypeSpec.classBuilder(metadata.getClassName() + "Dto")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Data.class)
                .addJavadoc(metadata.getClassJavadoc() + "\n");
        
        // 为每个字段生成属性
        for (FieldDeclaration field : metadata.getFields()) {
            VariableDeclarator variable = field.getVariables().get(0);
            String fieldName = variable.getNameAsString();
            TypeName fieldType = TemplateUtils.resolveTypeName(variable.getTypeAsString());
            
            FieldSpec.Builder fieldSpecBuilder = FieldSpec.builder(fieldType, fieldName, Modifier.PRIVATE);
            
            // 添加字段注释
            field.getJavadocComment().ifPresent(comment ->
                    fieldSpecBuilder.addJavadoc(comment.getContent() + "\n")
            );
            
            dtoBuilder.addField(fieldSpecBuilder.build());
        }
        
        JavaFile javaFile = JavaFile.builder(config.getFullPackage("dto"), dtoBuilder.build())
                .build();
        
        javaFile.writeTo(config.getOutputDir());
    }
    
    @Override
    public String getTemplateName() {
        return "dto";
    }
}