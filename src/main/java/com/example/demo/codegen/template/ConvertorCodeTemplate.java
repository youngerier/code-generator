package com.example.demo.codegen.template;


import com.example.demo.codegen.config.CodeGenConfig;
import com.example.demo.codegen.core.CodeTemplate;
import com.example.demo.codegen.core.EntityMetadata;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import org.mapstruct.Mapper;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Path;


public class ConvertorCodeTemplate implements CodeTemplate {

    @Override
    public void generate(EntityMetadata metadata, CodeGenConfig config) throws IOException {
        String entityName = metadata.getClassName();
        String packageName = metadata.getBasePackage();
        Path outputDir = config.getOutputDir();
        TypeName entityType = ClassName.get(packageName + ".dal.entity", entityName);
        ClassName dtoType = ClassName.get(packageName + ".dto", entityName + "Dto");

        MethodSpec toEntity = MethodSpec.methodBuilder("toEntity")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(entityType)
                .addParameter(dtoType, "dto")
                .addJavadoc("Converts a " + entityName + "Dto to a " + entityName + " entity.\n@param dto the DTO to convert\n@return the " + entityName + " entity\n")
                .build();

        MethodSpec toDto = MethodSpec.methodBuilder("toDto")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(dtoType)
                .addParameter(entityType, "entity")
                .addJavadoc("Converts a " + entityName + " entity to a " + entityName + "Dto.\n@param entity the entity to convert\n@return the " + entityName + "Dto\n")
                .build();

        TypeSpec mapper = TypeSpec.interfaceBuilder(entityName + "MapStructMapper")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Mapper.class)
                .addJavadoc("MapStruct mapper for converting between " + entityName + " and " + entityName + "Dto\n")
                .addMethod(toEntity)
                .addMethod(toDto)
                .build();

        JavaFile javaFile = JavaFile.builder(packageName + ".mapstruct", mapper)
                .build();

        javaFile.writeTo(outputDir);
    }

    @Override
    public String getTemplateName() {
        return "convertor";
    }
}
