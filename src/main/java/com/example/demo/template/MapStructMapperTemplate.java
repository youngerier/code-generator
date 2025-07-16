package com.example.demo.template;


import com.squareup.javapoet.*;
import org.mapstruct.Mapper;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MapStructMapperTemplate {

    private static final Path OUTPUT_DIR = Paths.get("src/main/java");

    public static void generate(String entityName, String packageName, String classJavadoc) throws IOException {
        TypeName entityType = ClassName.get(packageName, entityName);
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
                .addJavadoc(classJavadoc + "\nMapStruct mapper for converting between " + entityName + " and " + entityName + "Dto\n")
                .addMethod(toEntity)
                .addMethod(toDto)
                .build();

        JavaFile javaFile = JavaFile.builder(packageName + ".mapstruct", mapper)
                .build();

        javaFile.writeTo(OUTPUT_DIR);
    }
}