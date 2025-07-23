package com.example.demo.codegen.core;


import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

public class TemplateUtils {

    public static String getIdFieldType(ClassOrInterfaceDeclaration entityClass) {
        for (FieldDeclaration field : entityClass.getFields()) {
            if (field.isAnnotationPresent(com.mybatisflex.annotation.Id.class)) {
                VariableDeclarator variable = field.getVariables().get(0);
                return variable.getTypeAsString();
            }
        }
        throw new IllegalArgumentException("No @Id field found in " + entityClass.getNameAsString());
    }

    public static TypeName resolveTypeName(String type) {
        return switch (type) {
            case "Long" -> ClassName.get(Long.class);
            case "String" -> ClassName.get(String.class);
            case "Integer" -> ClassName.get(Integer.class);
            default -> ClassName.bestGuess(type);
        };
    }

    public static String getColumnName(FieldDeclaration field, String defaultName) {
        return field.getAnnotationByClass(com.mybatisflex.annotation.Column.class)
                .map(annotation -> {
                    if (annotation.isSingleMemberAnnotationExpr()) {
                        return annotation.asSingleMemberAnnotationExpr().getMemberValue().asStringLiteralExpr().asString();
                    } else if (annotation.isNormalAnnotationExpr()) {
                        return annotation.asNormalAnnotationExpr().getPairs().stream()
                                .filter(p -> p.getNameAsString().equals("value"))
                                .findFirst()
                                .map(p -> p.getValue().asStringLiteralExpr().asString())
                                .orElse(defaultName);
                    }
                    return defaultName;
                })
                .orElse(defaultName);
    }

    public static String toFieldName(String className) {
        if (className == null || className.isEmpty()) {
            return className;
        }
        StringBuilder fieldName = new StringBuilder();
        fieldName.append(Character.toLowerCase(className.charAt(0)));
        for (int i = 1; i < className.length(); i++) {
            char c = className.charAt(i);
            if (Character.isUpperCase(c)) {
                fieldName.append("_").append(Character.toLowerCase(c));
            } else {
                fieldName.append(c);
            }
        }
        return fieldName.toString();
    }

    public static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
