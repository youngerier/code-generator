package com.example.demo;

import com.example.demo.model.User;
import org.junit.jupiter.api.Test;

public class CodeGenTest {

    @Test
    public void test() {
        CodeGenerator.generate(User.class);
    }
}
