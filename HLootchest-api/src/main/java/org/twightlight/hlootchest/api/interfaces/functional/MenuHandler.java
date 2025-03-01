package org.twightlight.hlootchest.api.interfaces.functional;

@FunctionalInterface
public interface MenuHandler<T> {
    T createNew();
}
