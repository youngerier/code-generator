package com.example.demo.template;

import com.example.demo.TemplateUtils;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.paginate.Page;
import com.squareup.javapoet.*;
import org.springframework.stereotype.Service;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ServiceImplTemplate {

    private static final Path OUTPUT_DIR = Paths.get("src/main/java");

    public static void generate(String entityName, String packageName, String idType, String classJavadoc, List<FieldDeclaration> fields) throws IOException {
        TypeName entityType = ClassName.get(packageName, entityName);
        TypeName idTypeName = TemplateUtils.resolveTypeName(idType);
        ClassName mapperType = ClassName.get(packageName + ".dal.mapper", entityName + "FlexMapper");
        ClassName serviceType = ClassName.get(packageName + ".service", entityName + "Service");
        ClassName queryWrapperType = ClassName.get(QueryWrapper.class);
        ClassName pageType = ClassName.get(Page.class);
        ClassName queryType = ClassName.get(packageName + ".dto", entityName + "Query");
        ParameterizedTypeName listType = ParameterizedTypeName.get(ClassName.get(List.class), entityType);

        FieldSpec mapperField = FieldSpec.builder(mapperType, "mapper", Modifier.PRIVATE, Modifier.FINAL)
                .addJavadoc("The MyBatis-Flex mapper for " + entityName + "\n")
                .build();

        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(mapperType, "mapper")
                .addStatement("this.mapper = mapper")
                .addJavadoc("Constructs a new " + entityName + "ServiceImpl.\n@param mapper the MyBatis-Flex mapper\n")
                .build();

        MethodSpec selectById = MethodSpec.methodBuilder("selectById")
                .addModifiers(Modifier.PUBLIC)
                .returns(entityType)
                .addAnnotation(Override.class)
                .addParameter(idTypeName, "id")
                .addStatement("return mapper.selectOneById(id)")
                .addJavadoc("Selects a " + entityName + " by ID.\n@param id the ID of the " + entityName + "\n@return the " + entityName + " entity\n")
                .build();

        MethodSpec insert = MethodSpec.methodBuilder("insert")
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.VOID)
                .addAnnotation(Override.class)
                .addParameter(entityType, "entity")
                .addStatement("mapper.insert(entity)")
                .addJavadoc("Inserts a new " + entityName + ".\n@param entity the " + entityName + " to insert\n")
                .build();

        MethodSpec updateById = MethodSpec.methodBuilder("updateById")
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.VOID)
                .addAnnotation(Override.class)
                .addParameter(entityType, "entity")
                .addStatement("mapper.update(entity)")
                .addJavadoc("Updates a " + entityName + " by ID.\n@param entity the " + entityName + " to update\n")
                .build();

        MethodSpec deleteById = MethodSpec.methodBuilder("deleteById")
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.VOID)
                .addAnnotation(Override.class)
                .addParameter(idTypeName, "id")
                .addStatement("mapper.deleteById(id)")
                .addJavadoc("Deletes a " + entityName + " by ID.\n@param id the ID of the " + entityName + " to delete\n")
                .build();

        MethodSpec.Builder selectList = MethodSpec.methodBuilder("selectList")
                .addModifiers(Modifier.PUBLIC)
                .returns(listType)
                .addAnnotation(Override.class)
                .addParameter(queryType, "query")
                .addStatement("$T queryWrapper = $T.create().from($T.class)", queryWrapperType, queryWrapperType, entityType);

        MethodSpec.Builder selectPage = MethodSpec.methodBuilder("selectPage")
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(pageType, entityType))
                .addAnnotation(Override.class)
                .addParameter(TypeName.INT, "pageNumber")
                .addParameter(TypeName.INT, "pageSize")
                .addParameter(queryType, "query")
                .addStatement("$T queryWrapper = $T.create().from($T.class)", queryWrapperType, queryWrapperType, entityType)
                .addStatement("$T<$T> page = new $T<>(pageNumber, pageSize)", pageType, entityType, pageType);

        // 为每个字段添加 QueryWrapper 条件
        for (FieldDeclaration field : fields) {
            VariableDeclarator variable = field.getVariables().get(0);
            String fieldName = variable.getNameAsString();
            String columnName = TemplateUtils.getColumnName(field, fieldName);

            selectList.addStatement("if (query.get$N() != null) queryWrapper.and($T.$N.eq(query.get$N()))",
                    capitalize(fieldName), entityType, fieldName, capitalize(fieldName));
            selectPage.addStatement("if (query.get$N() != null) queryWrapper.and($T.$N.eq(query.get$N()))",
                    capitalize(fieldName), entityType, fieldName, capitalize(fieldName));
        }

        // 添加 deleted 字段的默认条件
        selectList.addStatement("queryWrapper.and($T.deleted.eq($T.FALSE))", entityType, ClassName.get(Boolean.class))
                .addStatement("return mapper.selectListByQuery(queryWrapper)")
                .addJavadoc("Queries a list of " + entityName + " based on conditions.\n@param query the query conditions\n@return the list of " + entityName + " entities\n");
        selectPage.addStatement("queryWrapper.and($T.deleted.eq($T.FALSE))", entityType, ClassName.get(Boolean.class))
                .addStatement("return mapper.paginate(page, queryWrapper)")
                .addJavadoc("Queries a paginated list of " + entityName + " based on conditions.\n@param pageNumber the page number\n@param pageSize the page size\n@param query the query conditions\n@return the paginated list of " + entityName + " entities\n");

        TypeSpec serviceImpl = TypeSpec.classBuilder(entityName + "ServiceImpl")
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(serviceType)
                .addAnnotation(Service.class)
                .addJavadoc(classJavadoc + "\nService implementation for " + entityName + "\n")
                .addField(mapperField)
                .addMethod(constructor)
                .addMethod(selectById)
                .addMethod(insert)
                .addMethod(updateById)
                .addMethod(deleteById)
                .addMethod(selectList.build())
                .addMethod(selectPage.build())
                .build();

        JavaFile javaFile = JavaFile.builder(packageName + ".service.impl", serviceImpl)
                .build();

        javaFile.writeTo(OUTPUT_DIR);
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}