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

        MethodSpec.Builder selectListBuilder = MethodSpec.methodBuilder("selectList")
                .addModifiers(Modifier.PUBLIC)
                .returns(listType)
                .addAnnotation(Override.class)
                .addStatement("$T queryWrapper = new $T()", queryWrapperType, queryWrapperType);

        MethodSpec.Builder selectPageBuilder = MethodSpec.methodBuilder("selectPage")
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(pageType, entityType))
                .addAnnotation(Override.class)
                .addParameter(TypeName.INT, "pageNumber")
                .addParameter(TypeName.INT, "pageSize")
                .addStatement("$T queryWrapper = new $T()", queryWrapperType, queryWrapperType)
                .addStatement("$T<$T> page = new $T<>(pageNumber, pageSize)", pageType, entityType, pageType);

        // 为每个字段添加参数和 QueryWrapper 条件
        for (FieldDeclaration field : fields) {
            VariableDeclarator variable = field.getVariables().get(0);
            String fieldName = variable.getNameAsString();
            TypeName fieldType = TemplateUtils.resolveTypeName(variable.getTypeAsString());
            String columnName = TemplateUtils.getColumnName(field, fieldName);

            selectListBuilder.addParameter(fieldType, fieldName)
                    .addJavadoc("@param $N the $N to filter by\n", fieldName, fieldName);
            selectPageBuilder.addParameter(fieldType, fieldName)
                    .addJavadoc("@param $N the $N to filter by\n", fieldName, fieldName);

            selectListBuilder.addStatement("if ($N != null) queryWrapper.where($T.$N.eq($N))",
                    fieldName, entityType, fieldName, fieldName);
            selectPageBuilder.addStatement("if ($N != null) queryWrapper.where($T.$N.eq($N))",
                    fieldName, entityType, fieldName, fieldName);
        }

        selectListBuilder.addStatement("return mapper.selectListByQuery(queryWrapper)")
                .addJavadoc("Queries a list of " + entityName + " based on conditions.\n")
                .addJavadoc("@return the list of " + entityName + " entities\n");
        selectPageBuilder.addStatement("return mapper.paginate(page, queryWrapper)")
                .addJavadoc("Queries a paginated list of " + entityName + " based on conditions.\n")
                .addJavadoc("@return the paginated list of " + entityName + " entities\n");

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
                .addMethod(selectListBuilder.build())
                .addMethod(selectPageBuilder.build())
                .build();

        JavaFile javaFile = JavaFile.builder(packageName + ".service.impl", serviceImpl)
                .build();

        javaFile.writeTo(OUTPUT_DIR);
    }
}