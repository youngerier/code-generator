package com.example.demo.template;


import com.example.demo.TemplateUtils;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.squareup.javapoet.*;
import lombok.Data;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class QueryTemplate {

    private static final Path OUTPUT_DIR = Paths.get("src/main/java");

    public static void generate(String entityName, String packageName, List<FieldDeclaration> fields, String classJavadoc) throws IOException {
        TypeSpec.Builder queryBuilder = TypeSpec.classBuilder(entityName + "Query")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Data.class)
                .addJavadoc(classJavadoc + "\nQuery object for filtering " + entityName + " entities\n");

        // 为每个字段生成属性和 getter/setter，并复制 Javadoc 注释
        for (FieldDeclaration field : fields) {
            VariableDeclarator variable = field.getVariables().get(0);
            String fieldName = variable.getNameAsString();
            TypeName fieldType = TemplateUtils.resolveTypeName(variable.getTypeAsString());

            FieldSpec.Builder fieldSpecBuilder = FieldSpec.builder(fieldType, fieldName, Modifier.PRIVATE);

            // 添加字段的 Javadoc 注释
            field.getJavadocComment().ifPresent(comment ->
                    fieldSpecBuilder.addJavadoc(comment.getContent() + "\n")
            );

            queryBuilder.addField(fieldSpecBuilder.build());
        }

        JavaFile javaFile = JavaFile.builder(packageName + ".dto", queryBuilder.build())
                .build();

        javaFile.writeTo(OUTPUT_DIR);
    }
}
