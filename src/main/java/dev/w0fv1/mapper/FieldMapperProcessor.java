package dev.w0fv1.mapper;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import jakarta.persistence.Entity;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@AutoService(Processor.class)
@SupportedAnnotationTypes("jakarta.persistence.Entity")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class FieldMapperProcessor extends AbstractProcessor {

    private Messager messager;
    private Filer filer;
    private Elements elementUtils;
    private Types typeUtils;

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        messager = env.getMessager();
        filer = env.getFiler();
        elementUtils = env.getElementUtils();
        typeUtils = env.getTypeUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(Entity.class)) {
            if (element.getKind() != ElementKind.CLASS) {
                messager.printMessage(Diagnostic.Kind.ERROR, "@Entity can only be applied to classes", element);
                continue;
            }

            TypeElement classElement = (TypeElement) element;
            String packageName = elementUtils.getPackageOf(classElement).getQualifiedName().toString();
            String className = classElement.getSimpleName().toString();
            String mapperClassName = className + "EntityFieldMapper";

            List<VariableElement> fields = new ArrayList<>();
            for (Element enclosed : classElement.getEnclosedElements()) {
                if (enclosed.getKind() == ElementKind.FIELD) {
                    fields.add((VariableElement) enclosed);
                }
            }

            if (fields.isEmpty()) continue;

            // 使用 JavaPoet 生成 Mapper 类
            TypeSpec.Builder mapperClassBuilder = TypeSpec.classBuilder(mapperClassName)
                    .addModifiers(Modifier.PUBLIC);

            for (VariableElement field : fields) {
                String fieldName = field.getSimpleName().toString();
                String fieldType = field.asType().toString();
                String capitalizedFieldName = capitalize(fieldName);
                String mapperInnerClassName = capitalizedFieldName + "FieldMapper";

                // 检查是否为 List 类型
                boolean isListType = isList(field);

                // 使用 JavaPoet 构建嵌套类
                TypeSpec.Builder innerClassBuilder = TypeSpec.classBuilder(mapperInnerClassName)
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .addSuperinterface(ParameterizedTypeName.get(
                                ClassName.get("dev.w0fv1.mapper", "Mapper"),
                                ClassName.get(packageName, className),
                                ClassName.bestGuess(fieldType)
                        ));

                // 构建 accept 方法
                MethodSpec.Builder acceptMethodBuilder = MethodSpec.methodBuilder("accept")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ClassName.get(packageName, className), "instance")
                        .addParameter(ClassName.bestGuess(fieldType), fieldName);

                if (isListType) {
                    acceptMethodBuilder.addStatement("if (instance.get$L() != null) {\n" +
                            "    instance.get$L().clear();\n" +
                            "    instance.get$L().addAll($L);\n" +
                            "} else {\n" +
                            "    instance.set$L($L);\n" +
                            "}", capitalizedFieldName, capitalizedFieldName, capitalizedFieldName, fieldName, capitalizedFieldName, fieldName);
                } else {
                    acceptMethodBuilder.addStatement("instance.set$L($L)", capitalizedFieldName, fieldName);
                }

                innerClassBuilder.addMethod(acceptMethodBuilder.build());
                mapperClassBuilder.addType(innerClassBuilder.build());
            }

            // 生成 Java 文件
            JavaFile javaFile = JavaFile.builder(packageName, mapperClassBuilder.build()).build();

            try {
                javaFile.writeTo(filer);
            } catch (IOException e) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Error generating " + mapperClassName + ": " + e.getMessage(), classElement);
            }
        }
        return true;
    }

    private boolean isList(VariableElement field) {
        TypeMirror listType = elementUtils.getTypeElement("java.util.List").asType();
        return typeUtils.isAssignable(typeUtils.erasure(field.asType()), typeUtils.erasure(listType));
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}
