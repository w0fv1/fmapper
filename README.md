# Fampper Library 0.0.1

Fampper 是一个用于自动生成实体字段映射器的库，适合在开发过程中使用注解处理器自动生成字段的类型安全映射器，避免手写 Setter 方法。该库生成的字段映射器可结合反射机制使用，方便地为实体字段设置值。

## 特性

- **自动生成字段映射器**：基于注解处理器自动为 `@Entity` 注解类生成字段映射类。
- **类型安全**：生成的映射类确保字段类型匹配，减少运行时错误。
- **反射兼容**：可结合反射机制动态设置字段值，适合灵活的数据填充场景。

## 注意

目前

## 安装

1. 在 `build.gradle.kts` 中添加依赖：

    ```kotlin
    repositories {
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/w0fv1/fampper")
            credentials {
                username = "YOUR_GITHUB_USERNAME"
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }

    dependencies {
        implementation("dev.w0fv1:fampper:0.0.1")
        annotationProcessor("dev.w0fv1:fampper:0.0.1")
    }
    ```

## 使用方法

1. **定义实体类**：定义一个使用 `@Entity` 注解的类，包含需要映射的字段。

    ```java
    package dev.w0fv1;

    import jakarta.persistence.Entity;
    import jakarta.persistence.Id;

    @Entity
    public class MyEntity {
        @Id
        private Long id;
        private String name;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
    ```

2. **自动生成映射器**：编译后，注解处理器会自动生成映射器类 `MyEntityEntityFieldMapper`。该类包含 `IdFieldMapper` 和 `NameFieldMapper` 嵌套类，用于设置 `id` 和 `name` 字段的值。

3. **使用生成的 Mapper 类进行字段赋值**：

   例如，通过 `NameFieldMapper` 和 `IdFieldMapper` 将数据设置到 `MyEntity` 对象上。

    ```java
    MyEntity entity = new MyEntity();
    MyEntityEntityFieldMapper.NameFieldMapper nameMapper = new MyEntityEntityFieldMapper.NameFieldMapper();
    nameMapper.accept(entity, "Hello");

    MyEntityEntityFieldMapper.IdFieldMapper idMapper = new MyEntityEntityFieldMapper.IdFieldMapper();
    idMapper.accept(entity, 123L);
    ```

   这会将 `name` 字段设置为 `"Hello"`，将 `id` 字段设置为 `123L`。

4. **结合反射使用**：

   您可以将生成的映射器类与注解（如 `@Set(entityMapper = MyEntityEntityFieldMapper.NameFieldMapper.class)`）配合使用，在需要的地方反射调用映射器的 `accept` 方法。例如：

    ```java
    FieldMapperProcessor.processField(MyEntity.class, "name", "Hello");
    ```

   使用反射可灵活处理字段映射，并减少手动编码工作。

## 许可证

Fampper 基于 Apache License 2.0 协议发布。详细信息请参见 `LICENSE` 文件。
