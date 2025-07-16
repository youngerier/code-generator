package com.example.demo.template;


import com.example.demo.TemplateUtils;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.paginate.Page;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ServiceInterfaceTemplate {

    private static final Path OUTPUT_DIR = Paths.get("src/main/java");

    public static void generate(String entityName, String packageName, String idType, String classJavadoc, List<FieldDeclaration> fields) throws IOException {
        TypeName entityType = ClassName.get(packageName, entityName);
        TypeName idTypeName = TemplateUtils.resolveTypeName(idType);
        ClassName pageType = ClassName.get(Page.class);
        ParameterizedTypeName listType = ParameterizedTypeName.get(ClassName.get(List.class), entityType);

        MethodSpec selectById = MethodSpec.methodBuilder("selectById")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(entityType)
                .addParameter(idTypeName, "id")
                .addJavadoc("Selects a " + entityName + " by ID.\n@param id the ID of the " + entityName + "\n@return the " + entityName + " entity\n")
                .build();

        MethodSpec insert = MethodSpec.methodBuilder("insert")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(TypeName.VOID)
                .addParameter(entityType, "entity")
                .addJavadoc("Inserts a new " + entityName + ".\n@param entity the " + entityName + " to insert\n")
                .build();

        MethodSpec updateById = MethodSpec.methodBuilder("updateById")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(TypeName.VOID)
                .addParameter(entityType, "entity")
                .addJavadoc("Updates a " + entityName + " by ID.\n@param entity the " + entityName + " to update\n")
                .build();

        MethodSpec deleteById = MethodSpec.methodBuilder("deleteById")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(TypeName.VOID)
                .addParameter(idTypeName, "id")
                .addJavadoc("Deletes a " + entityName + " by ID.\n@param id the ID of the " + entityName + " to delete\n")
                .build();

        MethodSpec.Builder selectListBuilder = MethodSpec.methodBuilder("selectList")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(listType)
                .addJavadoc("Queries a list of " + entityName + " based on conditions.\n");

        MethodSpec.Builder selectPageBuilder = MethodSpec.methodBuilder("selectPage")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(ParameterizedTypeName.get(pageType, entityType))
                .addParameter(TypeName.INT, "pageNumber")
                .addParameter(TypeName.INT, "pageSize")
                .addJavadoc("Queries a paginated list of " + entityName + " based on conditions.\n@param pageNumber the page number\n@param pageSize the page size\n");

        // 为每个字段添加参数
        for (FieldDeclaration field : fields) {
            VariableDeclarator variable = field.getVariables().get(0);
            String fieldName = variable.getNameAsString();
            TypeName fieldType = TemplateUtils.resolveTypeName(variable.getTypeAsString());
            selectListBuilder.addParameter(fieldType, fieldName)
                    .addJavadoc("@param $N the $N to filter by\n", fieldName, fieldName);
            selectPageBuilder.addParameter(fieldType, fieldName)
                    .addJavadoc("@param $N the $N to filter by\n", fieldName, fieldName);
        }

        selectListBuilder.addJavadoc("@return the list of " + entityName + " entities\n");
        selectPageBuilder.addJavadoc("@return the paginated list of " + entityName + " entities\n");

        TypeSpec service = TypeSpec.interfaceBuilder(entityName + "Service")
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc(classJavadoc + "\nService interface for " + entityName + "\n")
                .addMethod(selectById)
                .addMethod(insert)
                .addMethod(updateById)
                .addMethod(deleteById)
                .addMethod(selectListBuilder.build())
                .addMethod(selectPageBuilder.build())
                .build();

        JavaFile javaFile = JavaFile.builder(packageName + ".service", service)
                .build();

        javaFile.writeTo(OUTPUT_DIR);
    }
}