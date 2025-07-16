package com.example.demo.template;


import com.mybatisflex.core.BaseMapper;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MyBatisFlexMapperTemplate {

    private static final Path OUTPUT_DIR = Paths.get("src/main/java");

    public static void generate(String entityName, String packageName, String idType, String classJavadoc) throws IOException {
        TypeName entityType = ClassName.get(packageName + ".dal.entity", entityName);
        TypeName idTypeName = TemplateUtils.resolveTypeName(idType);

        TypeSpec mapper = TypeSpec.interfaceBuilder(entityName + "FlexMapper")
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ParameterizedTypeName.get(
                        ClassName.get(BaseMapper.class),
                        entityType))
                .addJavadoc(classJavadoc + "\nMyBatis-Flex Mapper interface for " + entityName + "\n")
                .build();

        JavaFile javaFile = JavaFile.builder(packageName + ".dal.mapper", mapper)
                .build();

        javaFile.writeTo(OUTPUT_DIR);
    }
}