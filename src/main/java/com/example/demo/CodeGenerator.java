package com.example.demo;

import com.example.demo.template.DtoTemplate;
import com.example.demo.template.MapStructMapperTemplate;
import com.example.demo.template.MyBatisFlexMapperTemplate;
import com.example.demo.template.QueryTemplate;
import com.example.demo.template.ServiceImplTemplate;
import com.example.demo.template.ServiceInterfaceTemplate;
import com.example.demo.template.TemplateUtils;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
public class CodeGenerator {

    private static final Path OUTPUT_DIR = Paths.get("src/main/java");
    private static final String ENTITY_PACKAGE = "dal.entity";

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
            String packageName = entityClass.getPackageName().substring(0, entityClass.getPackageName().lastIndexOf(ENTITY_PACKAGE));
            ClassOrInterfaceDeclaration parsedClass = cu.getClassByName(className)
                    .orElseThrow(() -> new IllegalArgumentException(className + " class not found"));

            // 获取类上的 Javadoc 注释
            String classJavadoc = parsedClass.getJavadocComment()
                    .map(JavadocComment::getContent)
                    .orElse("Generated class for " + className);

            // 获取 @Id 字段的类型
            String idType = TemplateUtils.getIdFieldType(parsedClass);

            // 获取所有字段，用于生成 DTO 和 QueryWrapper 参数
            List<FieldDeclaration> fields = parsedClass.getFields();

            // 生成代码
            DtoTemplate.generate(className, packageName, fields, classJavadoc);
            MyBatisFlexMapperTemplate.generate(className, packageName, idType, classJavadoc);
            ServiceInterfaceTemplate.generate(className, packageName, idType, classJavadoc, fields);
            ServiceImplTemplate.generate(className, packageName, idType, classJavadoc, fields);
            MapStructMapperTemplate.generate(className, packageName, classJavadoc);
            QueryTemplate.generate(className, packageName, fields, classJavadoc);

            log.info("Code generation completed successfully for {}", entityClass.getName());
        } catch (IOException e) {
            log.info("Error during code generation: {}", e.getMessage());
        }
    }
}