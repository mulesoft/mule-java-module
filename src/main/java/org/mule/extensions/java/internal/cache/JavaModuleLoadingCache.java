/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.internal.cache;

import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.mule.extensions.java.api.exception.ClassNotFoundModuleException;
import org.mule.extensions.java.api.exception.NoSuchConstructorModuleException;
import org.mule.extensions.java.api.exception.NoSuchMethodModuleException;
import org.mule.extensions.java.internal.parameters.ConstructorIdentifier;
import org.mule.extensions.java.internal.parameters.ExecutableIdentifier;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.core.api.util.ClassUtils;

/**
 * A thread safe loading cache implementation for {@link Class} and {@link Executable} elements, using its {@link Class#getName()}
 * and {@link ExecutableIdentifier} as keys respectively. This cache is intended to be used every time a given {@link Class}
 * {@link Constructor} or {@link Method} has to be loaded using reflection.
 *
 * @since 1.0
 */
public final class JavaModuleLoadingCache {

  private final String METHOD_IDENTIFIER = "%s#%s";

  private final Map<String, Class<?>> typesCache = new ConcurrentHashMap<>();
  private final Map<String, Executable> executablesCache = new ConcurrentHashMap<>();

  public Class<?> loadClass(String className) {
    return typesCache.computeIfAbsent(className, key -> {
      try {
        return ClassUtils.loadClass(className, Thread.currentThread().getContextClassLoader());
      } catch (ClassNotFoundException e) {
        throw new ClassNotFoundModuleException(e.getMessage(), e);
      }
    });
  }

  public Constructor getConstructor(ConstructorIdentifier id, Class<?> declaringClass,
                                    Map<String, TypedValue<Object>> args) {
    return (Constructor) executablesCache.computeIfAbsent(
                                                          String.format(METHOD_IDENTIFIER, declaringClass.getName(),
                                                                        id.getElementId()),
                                                          key -> stream(declaringClass.getConstructors())
                                                              .filter(c -> isPublic(c.getModifiers()))
                                                              .filter(id::matches)
                                                              .findFirst()
                                                              .orElseThrow(() -> new NoSuchConstructorModuleException(id,
                                                                                                                      declaringClass,
                                                                                                                      args)));
  }

  public Method getMethod(ExecutableIdentifier id, Class<?> clazz, Map<String, TypedValue<Object>> args, boolean expectStatic) {
    return (Method) executablesCache.computeIfAbsent(String.format(METHOD_IDENTIFIER, clazz.getName(), id.getElementId()),
                                                     key -> getPublicMethods(clazz, expectStatic).stream()
                                                         .filter(id::matches)
                                                         .findFirst()
                                                         .orElseThrow(() -> new NoSuchMethodModuleException(id, clazz,
                                                                                                            getPublicMethods(clazz,
                                                                                                                             expectStatic),
                                                                                                            args)));
  }

  private List<Method> getPublicMethods(Class<?> clazz, boolean expectStatic) {
    return stream(clazz.getMethods())
        .filter(m -> isPublic(m.getModifiers()))
        .filter(m -> expectStatic == isStatic(m.getModifiers()))
        .collect(toList());
  }

}
