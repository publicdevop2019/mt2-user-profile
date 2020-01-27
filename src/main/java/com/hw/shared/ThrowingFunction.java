package com.hw.shared;

@FunctionalInterface
public interface ThrowingFunction<T, R, E extends Exception> {
    R accept(T t) throws E;
}
