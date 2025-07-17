# 代码生成器重构说明

## 重构目标

本次重构的主要目标是：
- **架构简洁清晰**：采用模块化设计，职责分离
- **去除硬编码**：所有配置都可以外部化
- **易于扩展**：支持自定义模板和配置
- **配置灵活**：支持多种配置方式

## 新架构设计

### 核心组件

```
com.example.demo.codegen/
├── config/                 # 配置相关
│   ├── CodeGenConfig.java     # 配置类
│   └── CodeGenConfigLoader.java # 配置加载器
├── core/                   # 核心功能
│   ├── EntityMetadata.java    # 实体元数据
│   └── EntityParser.java      # 实体解析器
└── template/               # 模板相关
    ├── CodeTemplate.java      # 模板接口
    ├── DtoCodeTemplate.java   # DTO模板
    ├── MapperCodeTemplate.java # Mapper模板
    └── ServiceCodeTemplate.java # Service模板
```

### 设计原则

1. **单一职责**：每个类只负责一个功能
2. **开闭原则**：对扩展开放，对修改关闭
3. **依赖注入**：通过构造器注入依赖
4. **配置驱动**：行为由配置决定

## 使用方式

### 1. 基本使用（向后兼容）

```java
// 使用默认配置
CodeGenerator.generate(User.class);
```

### 2. 使用配置文件

```java
// 从默认配置文件加载
CodeGenerator generator = CodeGenerator.fromConfigFile();
generator.generate(User.class);

// 从指定配置文件加载
CodeGenerator generator = CodeGenerator.fromConfigFile("custom-codegen.properties");
generator.generate(User.class);
```

### 3. 程序化配置

```java
CodeGenConfig config = CodeGenConfig.builder()
    .basePackage("com.example.demo")
    .outputDir(Paths.get("src/main/java"))
    .entityPackageSuffix(".model.entity")
    .build();

CodeGenerator generator = new CodeGenerator(config);
generator.generate(User.class);
```

### 4. 自定义模板

```java
// 创建自定义模板
public class CustomTemplate implements CodeTemplate {
    @Override
    public void generate(EntityMetadata metadata, CodeGenConfig config) {
        // 自定义生成逻辑
    }
    
    @Override
    public String getTemplateName() {
        return "custom";
    }
}

// 添加到生成器
CodeGenerator generator = new CodeGenerator();
generator.addTemplate(new CustomTemplate());
generator.generate(User.class);
```

## 配置说明

### 配置文件格式 (codegen.properties)

```properties
# 基础配置
codegen.output.dir=src/main/java
codegen.base.package=com.example.demo.model
codegen.entity.package.suffix=.dal.entity

# 包名配置
codegen.package.dto=.dto
codegen.package.mapper=.mapper.flex
codegen.package.service=.service
codegen.package.serviceImpl=.service.impl
codegen.package.query=.query

# 模板启用配置
codegen.template.dto.enabled=true
codegen.template.mapper.enabled=true
codegen.template.service.enabled=true
codegen.template.serviceImpl.enabled=false
codegen.template.mapstruct.enabled=false
codegen.template.query.enabled=false
```

### 配置项说明

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `codegen.output.dir` | 代码输出目录 | `src/main/java` |
| `codegen.base.package` | 基础包名 | `com.example.demo.model` |
| `codegen.entity.package.suffix` | 实体包后缀 | `.dal.entity` |
| `codegen.package.*` | 各层包名后缀 | 见配置文件 |
| `codegen.template.*.enabled` | 模板启用开关 | `true` |

## 扩展指南

### 添加新模板

1. 实现 `CodeTemplate` 接口
2. 在 `initializeTemplates()` 方法中注册
3. 在配置文件中添加启用开关

```java
public class NewTemplate implements CodeTemplate {
    @Override
    public void generate(EntityMetadata metadata, CodeGenConfig config) {
        // 实现生成逻辑
    }
    
    @Override
    public String getTemplateName() {
        return "new-template";
    }
}
```

### 自定义配置加载

```java
public class CustomConfigLoader {
    public static CodeGenConfig loadFromDatabase() {
        // 从数据库加载配置
        return CodeGenConfig.builder()
            .basePackage("custom.package")
            .build();
    }
}
```

## 重构前后对比

### 重构前的问题

- 硬编码的包名和路径
- 所有逻辑耦合在一个类中
- 难以扩展和自定义
- 配置不灵活

### 重构后的优势

- ✅ 配置完全外部化
- ✅ 模块化设计，职责清晰
- ✅ 易于扩展新模板
- ✅ 支持多种配置方式
- ✅ 保持向后兼容
- ✅ 更好的错误处理和日志

## 最佳实践

1. **使用配置文件**：将配置外部化，便于不同环境使用
2. **模板选择性启用**：只启用需要的模板，提高生成效率
3. **自定义包结构**：根据项目需要调整包名结构
4. **扩展模板**：为特殊需求创建自定义模板
5. **错误处理**：关注日志输出，及时发现问题

## 后续计划

- [ ] 添加更多内置模板（Controller、Query等）
- [ ] 支持模板文件外部化
- [ ] 添加代码格式化选项
- [ ] 支持增量生成
- [ ] 添加单元测试