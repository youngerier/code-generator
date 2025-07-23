package com.example.demo.codegen.template;

import com.example.demo.codegen.config.CodeGenConfig;
import com.example.demo.codegen.core.CodeTemplate;
import com.example.demo.codegen.core.EntityMetadata;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.JavaFile;

import javax.lang.model.element.Modifier;
import java.io.IOException;

/**
 * Repository代码模板
 */
public class RepositoryCodeTemplate implements CodeTemplate {

    @Override
    public void generate(EntityMetadata metadata, CodeGenConfig config) throws IOException {
        ClassName entityType = ClassName.get(metadata.getPackageName(), metadata.getClassName());
        ClassName baseRepositoryType = ClassName.get("org.springframework.data.jpa.repository", "JpaRepository");
        ParameterizedTypeName repositoryInterface = ParameterizedTypeName.get(baseRepositoryType, entityType, ClassName.get(Long.class));

        TypeSpec repositorySpec = TypeSpec.interfaceBuilder(metadata.getClassName() + "Repository")
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(repositoryInterface)
                .addJavadoc(metadata.getClassJavadoc() + " Repository接口\n")
                .build();

        JavaFile javaFile = JavaFile.builder(config.getFullPackage("repository"), repositorySpec)
                .build();

        javaFile.writeTo(config.getOutputDir());
    }

    @Override
    public String getTemplateName() {
        return "repository";
    }
}
