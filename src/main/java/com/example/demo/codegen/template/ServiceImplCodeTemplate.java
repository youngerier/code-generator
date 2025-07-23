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

/**
 * Service接口代码模板
 */
public class ServiceImplCodeTemplate implements CodeTemplate {
    
    @Override
    public void generate(EntityMetadata metadata, CodeGenConfig config) throws IOException {
        TypeName entityType = ClassName.get(metadata.getPackageName(), metadata.getClassName());
        TypeName idTypeName = TemplateUtils.resolveTypeName(metadata.getIdType());
        ClassName queryType = ClassName.get(config.getFullPackage("dto"), metadata.getClassName() + "Query");
        ClassName pageType = ClassName.get(Page.class);
        ParameterizedTypeName listType = ParameterizedTypeName.get(ClassName.get(List.class), entityType);
        
        TypeSpec.Builder serviceBuilder = TypeSpec.interfaceBuilder(metadata.getClassName() + "Service")
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc(metadata.getClassJavadoc() + "\nService interface for " + metadata.getClassName() + "\n");
        
        // 基础CRUD方法
        serviceBuilder.addMethod(createSelectByIdMethod(entityType, idTypeName, metadata.getClassName()));
        serviceBuilder.addMethod(createInsertMethod(entityType, metadata.getClassName()));
        serviceBuilder.addMethod(createUpdateByIdMethod(entityType, metadata.getClassName()));
        serviceBuilder.addMethod(createDeleteByIdMethod(idTypeName, metadata.getClassName()));
        serviceBuilder.addMethod(createSelectListMethod(listType, queryType, metadata.getClassName()));
        serviceBuilder.addMethod(createSelectPageMethod(pageType, entityType, queryType, metadata.getClassName()));
        
        JavaFile javaFile = JavaFile.builder(config.getFullPackage("service"), serviceBuilder.build())
                .build();
        
        javaFile.writeTo(config.getOutputDir());
    }
    
    private MethodSpec createSelectByIdMethod(TypeName entityType, TypeName idTypeName, String entityName) {
        return MethodSpec.methodBuilder("selectById")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(entityType)
                .addParameter(idTypeName, "id")
                .addJavadoc("根据ID查询$L\n@param id $L的ID\n@return $L实体\n", entityName, entityName, entityName)
                .build();
    }
    
    private MethodSpec createInsertMethod(TypeName entityType, String entityName) {
        return MethodSpec.methodBuilder("insert")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(TypeName.VOID)
                .addParameter(entityType, "entity")
                .addJavadoc("新增$L\n@param entity 要新增的$L\n", entityName, entityName)
                .build();
    }
    
    private MethodSpec createUpdateByIdMethod(TypeName entityType, String entityName) {
        return MethodSpec.methodBuilder("updateById")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(TypeName.VOID)
                .addParameter(entityType, "entity")
                .addJavadoc("根据ID更新$L\n@param entity 要更新的$L\n", entityName, entityName)
                .build();
    }
    
    private MethodSpec createDeleteByIdMethod(TypeName idTypeName, String entityName) {
        return MethodSpec.methodBuilder("deleteById")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(TypeName.VOID)
                .addParameter(idTypeName, "id")
                .addJavadoc("根据ID删除$L\n@param id 要删除的$L的ID\n", entityName, entityName)
                .build();
    }
    
    private MethodSpec createSelectListMethod(ParameterizedTypeName listType, ClassName queryType, String entityName) {
        return MethodSpec.methodBuilder("selectList")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(listType)
                .addParameter(queryType, "query")
                .addJavadoc("根据条件查询$L列表\n@param query 查询条件\n@return $L实体列表\n", entityName, entityName)
                .build();
    }
    
    private MethodSpec createSelectPageMethod(ClassName pageType, TypeName entityType, ClassName queryType, String entityName) {
        return MethodSpec.methodBuilder("selectPage")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(ParameterizedTypeName.get(pageType, entityType))
                .addParameter(TypeName.INT, "pageNumber")
                .addParameter(TypeName.INT, "pageSize")
                .addParameter(queryType, "query")
                .addJavadoc("根据条件分页查询$L\n@param pageNumber 页码\n@param pageSize 页大小\n@param query 查询条件\n@return $L分页结果\n", entityName, entityName)
                .build();
    }
    
    @Override
    public String getTemplateName() {
        return "service";
    }
}
