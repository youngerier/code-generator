package com.example.demo.codegen.template;


import com.example.demo.codegen.config.CodeGenConfig;
import com.example.demo.codegen.core.CodeTemplate;
import com.example.demo.codegen.core.EntityMetadata;
import com.example.demo.codegen.core.TemplateUtils;
import com.mybatisflex.core.paginate.Page;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.List;

public class ServiceCodeTemplate implements CodeTemplate {


    @Override
    public void generate(EntityMetadata metadata, CodeGenConfig config) throws IOException {
        String entityName = metadata.getClassName();
        String packageName = metadata.getBasePackage();

        TypeName entityType = ClassName.get(packageName + ".dal.entity", entityName);
        TypeName idTypeName = TemplateUtils.resolveTypeName(metadata.getIdType());
        ClassName queryType = ClassName.get(packageName + ".dto", entityName + "Query");
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

        MethodSpec selectList = MethodSpec.methodBuilder("selectList")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(listType)
                .addParameter(queryType, "query")
                .addJavadoc("Queries a list of " + entityName + " based on conditions, excluding deleted records.\n@param query the query conditions\n@return the list of " + entityName + " entities\n")
                .build();

        MethodSpec selectPage = MethodSpec.methodBuilder("selectPage")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(ParameterizedTypeName.get(pageType, entityType))
                .addParameter(TypeName.INT, "pageNumber")
                .addParameter(TypeName.INT, "pageSize")
                .addParameter(queryType, "query")
                .addJavadoc("Queries a paginated list of " + entityName + " based on conditions, excluding deleted records.\n@param pageNumber the page number\n@param pageSize the page size\n@param query the query conditions\n@return the paginated list of " + entityName + " entities\n")
                .build();

        TypeSpec service = TypeSpec.interfaceBuilder(entityName + "Service")
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc("Service interface for " + entityName + "\n")
                .addMethod(selectById)
                .addMethod(insert)
                .addMethod(updateById)
                .addMethod(deleteById)
                .addMethod(selectList)
                .addMethod(selectPage)
                .build();

        JavaFile javaFile = JavaFile.builder(packageName + ".service", service)
                .build();

        javaFile.writeTo(config.getOutputDir());
    }

    @Override
    public String getTemplateName() {
        return "service";
    }
}
