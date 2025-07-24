package com.example.demo.codegen.template;

import com.example.demo.codegen.config.CodeGenConfig;
import com.example.demo.codegen.core.CodeTemplate;
import com.example.demo.codegen.core.EntityMetadata;
import com.example.demo.codegen.core.TemplateUtils;
import com.squareup.javapoet.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.List;

/**
 * Controller代码模板
 */
public class ControllerCodeTemplate implements CodeTemplate {

    @Override
    public void generate(EntityMetadata metadata, CodeGenConfig config) throws IOException {
        ClassName serviceType = ClassName.get(config.getFullPackage("service"), metadata.getClassName() + "Service");
        TypeName entityType = ClassName.get(metadata.getPackageName(), metadata.getClassName());
        TypeName idTypeName = TemplateUtils.resolveTypeName(metadata.getIdType());
        ClassName dtoType = ClassName.get(config.getFullPackage("dto"), metadata.getClassName() + "Dto");
        ClassName queryType = ClassName.get(config.getFullPackage("dto"), metadata.getClassName() + "Query");
        ClassName pageType = ClassName.get("com.mybatisflex.core.paginate", "Page");
        ParameterizedTypeName listDtoType = ParameterizedTypeName.get(ClassName.get(List.class), dtoType);
        ParameterizedTypeName pageDtoType = ParameterizedTypeName.get(pageType, dtoType);

        TypeSpec.Builder controllerBuilder = TypeSpec.classBuilder(metadata.getClassName() + "Controller")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(RestController.class)
                .addAnnotation(AnnotationSpec.builder(RequestMapping.class)
                    .addMember("value", "$S", "/api/" + metadata.getClassName().toLowerCase())
                    .build())
                .addJavadoc(metadata.getClassJavadoc() + " REST API Controller\n")
                .addField(FieldSpec.builder(serviceType, "service", Modifier.PRIVATE)
                        .addAnnotation(Autowired.class)
                        .build());

        // 添加CRUD接口方法
        controllerBuilder.addMethod(createGetByIdMethod(entityType, idTypeName, metadata.getClassName()));
        controllerBuilder.addMethod(createCreateMethod(dtoType, metadata.getClassName()));
        controllerBuilder.addMethod(createUpdateMethod(dtoType, metadata.getClassName()));
        controllerBuilder.addMethod(createDeleteMethod(idTypeName, metadata.getClassName()));
        controllerBuilder.addMethod(createListMethod(listDtoType, queryType, metadata.getClassName()));
        controllerBuilder.addMethod(createPageMethod(pageDtoType, queryType, metadata.getClassName()));

        JavaFile javaFile = JavaFile.builder(config.getFullPackage("controller"), controllerBuilder.build())
                .build();

        javaFile.writeTo(config.getOutputDir());
    }

    private MethodSpec createGetByIdMethod(TypeName returnType, TypeName idTypeName, String entityName) {
        return MethodSpec.methodBuilder("getById")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(GetMapping.class).addMember("value", "$S", "/{id}").build())
                .returns(returnType)
                .addParameter(idTypeName, "id", Modifier.FINAL)
                .addStatement("return service.selectById(id)")
                .addJavadoc("查询$L详情\n@param id $LID\n@return $L详情\n", entityName, entityName, entityName)
                .build();
    }

    private MethodSpec createCreateMethod(TypeName dtoType, String entityName) {
        return MethodSpec.methodBuilder("create")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(PostMapping.class)
                .returns(TypeName.VOID)
                .addParameter(ParameterSpec.builder(dtoType, "dto", Modifier.FINAL).addAnnotation(AnnotationSpec.builder(RequestBody.class).addMember("required", "$L", true).build()).build())
                .addStatement("service.insert(dto)")
                .addJavadoc("创建$L\n@param dto $L数据传输对象\n", entityName, entityName)
                .build();
    }

    private MethodSpec createUpdateMethod(TypeName dtoType, String entityName) {
        return MethodSpec.methodBuilder("update")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(PutMapping.class)
                .returns(TypeName.VOID)
                .addParameter(ParameterSpec.builder(dtoType, "dto", Modifier.FINAL).addAnnotation(AnnotationSpec.builder(RequestBody.class).addMember("required", "$L", true).build()).build())
                .addStatement("service.updateById(dto)")
                .addJavadoc("更新$L\n@param dto $L数据传输对象\n", entityName, entityName)
                .build();
    }

    private MethodSpec createDeleteMethod(TypeName idTypeName, String entityName) {
        return MethodSpec.methodBuilder("delete")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(DeleteMapping.class).addMember("value", "$S", "/{id}").build())
                .returns(TypeName.VOID)
                .addParameter(idTypeName, "id", Modifier.FINAL)
                .addStatement("service.deleteById(id)")
                .addJavadoc("删除$L\n@param id $LID\n", entityName, entityName)
                .build();
    }

    private MethodSpec createListMethod(TypeName returnType, TypeName queryType, String entityName) {
        return MethodSpec.methodBuilder("list")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(GetMapping.class).addMember("value", "$S", "/list").build())
                .returns(returnType)
                .addParameter(ParameterSpec.builder(queryType, "query").addAnnotation(AnnotationSpec.builder(RequestParam.class).addMember("required", "$L", false).build()).build())
                .addStatement("return service.selectList(query != null ? query : new $T()", queryType)
                .addJavadoc("查询$L列表\n@param query 查询条件\n@return $L列表\n", entityName, entityName)
                .build();
    }

    private MethodSpec createPageMethod(TypeName returnType, TypeName queryType, String entityName) {
        return MethodSpec.methodBuilder("page")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(GetMapping.class).addMember("value", "$S", "/page").build())
                .returns(returnType)
                .addParameter(ParameterSpec.builder(TypeName.INT, "pageNumber").addAnnotation(AnnotationSpec.builder(RequestParam.class).addMember("defaultValue", "$S", "1").build()).build())
                .addParameter(ParameterSpec.builder(TypeName.INT, "pageSize").addAnnotation(AnnotationSpec.builder(RequestParam.class).addMember("defaultValue", "$S", "10").build()).build())
                .addParameter(ParameterSpec.builder(queryType, "query").addAnnotation(AnnotationSpec.builder(RequestParam.class).addMember("required", "$L", false).build()).build())
                .addStatement("return service.selectPage(pageNumber, pageSize, query != null ? query : new $T())", queryType)
                .addJavadoc("分页查询$L\n@param pageNumber 页码\n@param pageSize 每页大小\n@param query 查询条件\n@return $L分页结果\n", entityName, entityName)
                .build();
    }

    @Override
    public String getTemplateName() {
        return "controller";
    }
}
