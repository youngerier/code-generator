package com.example.demo;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.JavadocComment;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.paginate.Page;
import com.squareup.javapoet.*;
import com.mybatisflex.core.BaseMapper;
import org.springframework.stereotype.Service;
import org.mapstruct.Mapper;
import lombok.Data;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class CodeGenerator {

    private static final Path OUTPUT_DIR = Paths.get("src/main/java");

    public static void generate(Class<?> entityClass) {
        try {
            // 动态获取源码文件路径
            String classPath = entityClass.getName().replace(".", "/") + ".java";
            String sourcePath = OUTPUT_DIR + "/" + classPath;
            File sourceFile = new File(sourcePath);
            if (!sourceFile.exists()) {
                throw new IllegalArgumentException("Source file not found: " + sourcePath);
            }

            // 解析源码
            CompilationUnit cu = StaticJavaParser.parse(sourceFile);

            // 获取类和元数据
            String className = entityClass.getSimpleName();
            String packageName = entityClass.getPackageName();
            ClassOrInterfaceDeclaration parsedClass = cu.getClassByName(className)
                    .orElseThrow(() -> new IllegalArgumentException(className + " class not found"));

            // 获取类上的 Javadoc 注释
            String classJavadoc = parsedClass.getJavadocComment()
                    .map(JavadocComment::getContent)
                    .orElse("Generated class for " + className);

            // 获取 @Id 字段的类型
            String idType = getIdFieldType(parsedClass);

            // 获取所有字段，用于生成 DTO 和 QueryWrapper 参数
            List<FieldDeclaration> fields = parsedClass.getFields();

            // 生成代码
            generateDto(className, packageName, fields, classJavadoc);
            generateMyBatisFlexMapper(className, packageName, idType, classJavadoc);
            generateServiceInterface(className, packageName, idType, classJavadoc, fields);
            generateServiceImpl(className, packageName, idType, classJavadoc, fields);
            generateMapStructMapper(className, packageName, classJavadoc);

            System.out.println("Code generation completed successfully for " + entityClass.getName());
        } catch (IOException e) {
            System.err.println("Error during code generation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String getIdFieldType(ClassOrInterfaceDeclaration entityClass) {
        for (FieldDeclaration field : entityClass.getFields()) {
            if (field.isAnnotationPresent(com.mybatisflex.annotation.Id.class)) {
                VariableDeclarator variable = field.getVariables().get(0);
                return variable.getTypeAsString();
            }
        }
        throw new IllegalArgumentException("No @Id field found in " + entityClass.getNameAsString());
    }

    private static TypeName resolveTypeName(String type) {
        return switch (type) {
            case "Long" -> ClassName.get(Long.class);
            case "String" -> ClassName.get(String.class);
            case "Integer" -> ClassName.get(Integer.class);
            default -> ClassName.bestGuess(type);
        };
    }

    private static void generateDto(String entityName, String packageName, List<FieldDeclaration> fields, String classJavadoc) throws IOException {
        TypeSpec.Builder dtoBuilder = TypeSpec.classBuilder(entityName + "Dto")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Data.class)
                .addJavadoc(classJavadoc + "\n");

        // 为每个字段生成属性，并复制 Javadoc 注释
        for (FieldDeclaration field : fields) {
            VariableDeclarator variable = field.getVariables().get(0);
            String fieldName = variable.getNameAsString();
            TypeName fieldType = resolveTypeName(variable.getTypeAsString());

            FieldSpec.Builder fieldSpecBuilder = FieldSpec.builder(fieldType, fieldName, Modifier.PRIVATE);

            // 添加字段的 Javadoc 注释
            field.getJavadocComment().ifPresent(comment ->
                    fieldSpecBuilder.addJavadoc(comment.getContent() + "\n")
            );

            dtoBuilder.addField(fieldSpecBuilder.build());
        }

        JavaFile javaFile = JavaFile.builder(packageName + ".dto", dtoBuilder.build())
                .build();

        javaFile.writeTo(OUTPUT_DIR);
    }

    private static void generateMyBatisFlexMapper(String entityName, String packageName, String idType, String classJavadoc) throws IOException {
        TypeName entityType = ClassName.get(packageName, entityName);
        TypeName idTypeName = resolveTypeName(idType);

        TypeSpec mapper = TypeSpec.interfaceBuilder(entityName + "Mapper")
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ParameterizedTypeName.get(
                        ClassName.get(BaseMapper.class),
                        entityType))
                .addJavadoc(classJavadoc + "\nMyBatis-Flex Mapper interface for " + entityName + "\n")
                .build();

        JavaFile javaFile = JavaFile.builder(packageName + ".mapper.flex", mapper)
                .build();

        javaFile.writeTo(OUTPUT_DIR);
    }

    private static void generateServiceInterface(String entityName, String packageName, String idType, String classJavadoc, List<FieldDeclaration> fields) throws IOException {
        TypeName entityType = ClassName.get(packageName, entityName);
        TypeName idTypeName = resolveTypeName(idType);
        ClassName queryWrapperType = ClassName.get(QueryWrapper.class);
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
            TypeName fieldType = resolveTypeName(variable.getTypeAsString());
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

    private static void generateServiceImpl(String entityName, String packageName, String idType, String classJavadoc, List<FieldDeclaration> fields) throws IOException {
        TypeName entityType = ClassName.get(packageName, entityName);
        TypeName idTypeName = resolveTypeName(idType);
        ClassName mapperType = ClassName.get(packageName + ".mapper.flex", entityName + "Mapper");
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
            TypeName fieldType = resolveTypeName(variable.getTypeAsString());
            String columnName = field.getAnnotationByClass(com.mybatisflex.annotation.Column.class)
                    .map(annotation -> {
                        if (annotation.isSingleMemberAnnotationExpr()) {
                            return annotation.asSingleMemberAnnotationExpr().getMemberValue().asStringLiteralExpr().asString();
                        } else if (annotation.isNormalAnnotationExpr()) {
                            return annotation.asNormalAnnotationExpr().getPairs().stream()
                                    .filter(p -> p.getNameAsString().equals("value"))
                                    .findFirst()
                                    .map(p -> p.getValue().asStringLiteralExpr().asString())
                                    .orElse(fieldName);
                        }
                        return fieldName;
                    })
                    .orElse(fieldName);

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

    private static void generateMapStructMapper(String entityName, String packageName, String classJavadoc) throws IOException {
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

        TypeSpec mapper = TypeSpec.interfaceBuilder(entityName + "MapStruct")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Mapper.class)
                .addJavadoc(classJavadoc + "\nMapStruct mapper for converting between " + entityName + " and " + entityName + "Dto\n")
                .addMethod(toEntity)
                .addMethod(toDto)
                .build();

        JavaFile javaFile = JavaFile.builder(packageName + ".mapper.mapstruct", mapper)
                .build();

        javaFile.writeTo(OUTPUT_DIR);
    }
}