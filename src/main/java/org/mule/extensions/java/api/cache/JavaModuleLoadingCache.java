/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.api.cache;

import static java.util.Optional.ofNullable;
import static org.mule.runtime.core.api.util.ClassUtils.withContextClassLoader;
import org.mule.extensions.java.internal.parameters.ExecutableIdentifier;
import org.mule.extensions.java.internal.parameters.MethodIdentifier;
import org.mule.runtime.core.api.util.ClassUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * A thread safe loading cache implementation for {@link Class} and {@link Executable} elements,
 * using its {@link Class#getName()} and {@link ExecutableIdentifier} as keys respectively.
 * This cache is intended to be used every time a given {@link Class} {@link Constructor} or {@link Method}
 * has to be loaded using reflection.
 *
 * @since 1.0
 */
public final class JavaModuleLoadingCache {

  private final Map<String, Class<?>> typesCache = new ConcurrentHashMap<>();
  private final Map<String, Executable> executablesCache = new ConcurrentHashMap<>();

  public Optional<Class<?>> loadClass(String className) {
    return ofNullable(typesCache.computeIfAbsent(className, key -> {
      try {
        return ClassUtils.loadClass(className, Thread.currentThread().getContextClassLoader());
      } catch (ClassNotFoundException e) {
        return null;
      }
    }));
  }

  public <T extends Executable> Optional<T> getExecutable(ExecutableIdentifier id, Function<String, T> loader) {
    return ofNullable((T) executablesCache.computeIfAbsent(id.getElementId(),
                                                           key -> withContextClassLoader(Thread.currentThread()
                                                               .getContextClassLoader(),
                                                                                         () -> loader.apply(key))));
  }

}
