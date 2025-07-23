package com.example.demo.codegen.template;


import com.example.demo.codegen.config.CodeGenConfig;
import com.example.demo.codegen.core.CodeTemplate;
import com.example.demo.codegen.core.EntityMetadata;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class QueryCodeTemplate implements CodeTemplate {

    private static final Path OUTPUT_DIR = Paths.get("src/main/java");


    @Override
    public void generate(EntityMetadata metadata, CodeGenConfig config) throws IOException {

    }

    @Override
    public String getTemplateName() {
        return "QueryDTO";
    }
}
