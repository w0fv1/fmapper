package dev.w0fv1.mapper;

import java.lang.annotation.*;

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldMapper {
    Class<? extends Mapper<?, ?>> setter();


}

// 定义 Mapper 接口
