package dev.w0fv1.mapper;

public interface Mapper<V, T> {
    void accept(V v,T t);
}