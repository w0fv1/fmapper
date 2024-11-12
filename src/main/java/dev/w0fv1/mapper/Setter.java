package dev.w0fv1.mapper;

public interface Setter<V, T> {
        void accept(V v, T t);
    }