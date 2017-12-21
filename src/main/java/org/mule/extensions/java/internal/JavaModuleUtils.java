/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.internal;

import static java.lang.String.format;
import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.mule.runtime.core.api.util.ClassUtils.isInstance;
import static org.mule.runtime.core.api.util.ClassUtils.withContextClassLoader;
import org.mule.extensions.java.api.cache.JavaModuleLoadingCache;
import org.mule.extensions.java.api.exception.ArgumentMismatchModuleException;
import org.mule.extensions.java.api.exception.ClassNotFoundModuleException;
import org.mule.extensions.java.api.exception.InvocationModuleException;
import org.mule.extensions.java.api.exception.NoSuchMethodModuleException;
import org.mule.extensions.java.api.exception.WrongTypeModuleException;
import org.mule.extensions.java.internal.parameters.ExecutableIdentifier;
import org.mule.runtime.api.metadata.TypedValue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Utility class to common functions across operations
 *
 * @since 1.0
 */
public final class JavaModuleUtils {

  public static void validateType(String clazz, Object instance, boolean acceptSubtypes, JavaModuleLoadingCache cache)
      throws ClassNotFoundModuleException, WrongTypeModuleException {

    Class<?> declaredClass = cache.loadClass(clazz)
        .orElseThrow(() -> new ClassNotFoundModuleException(format("Failed to load Class with name [%s] ", clazz)));

    boolean isValid = acceptSubtypes
        ? isInstance(declaredClass, instance)
        : declaredClass.equals(instance.getClass());

    if (!isValid) {
      throw new WrongTypeModuleException(clazz, instance.getClass().getName());
    }
  }

  public static Object invokeMethod(Method method, Map<String, TypedValue<Object>> args,
                                    Object instance, Supplier<String> failureMessageProvider)
      throws ArgumentMismatchModuleException, InvocationModuleException, NoSuchMethodModuleException {

    return withContextClassLoader(Thread.currentThread().getContextClassLoader(), () -> {
      try {
        List<Object> sortedArgs = JavaModuleUtils.getSortedArgs(args, method.getParameters());
        if (sortedArgs.size() == method.getParameters().length) {
          return method.invoke(instance, sortedArgs.toArray());
        }

        throw new ArgumentMismatchModuleException(failureMessageProvider.get(), method, args);
      } catch (IllegalArgumentException e) {
        throw new ArgumentMismatchModuleException(failureMessageProvider.get(), method, args, e);

      } catch (InvocationTargetException e) {
        throw new InvocationModuleException(failureMessageProvider.get(), args, e);
      }
    });
  }

  public static List<Object> getSortedArgs(Map<String, TypedValue<Object>> args, Parameter[] parameters) {
    List<Object> sortedArgs = new ArrayList<>(parameters.length);
    for (Parameter parameter : parameters) {
      TypedValue<Object> value = args.get(parameter.getName());
      if (value == null) {
        break;
      }
      sortedArgs.add(value.getValue());
    }

    return sortedArgs;
  }

  public static Method findMethod(ExecutableIdentifier id, Class<?> clazz, boolean expectStatic,
                                  Map<String, TypedValue<Object>> args, JavaModuleLoadingCache cache)
      throws NoSuchMethodModuleException {
    Function<String, Method> loader = key -> getPublicMethods(clazz, expectStatic).stream()
        .filter(id::matches)
        .findFirst()
        .orElse(null);

    return cache.getExecutable(id, loader)
        .orElseThrow(() -> new NoSuchMethodModuleException(id, clazz, getPublicMethods(clazz, expectStatic), args));
  }

  public static List<Method> getPublicMethods(Class<?> clazz, boolean expectStatic) {
    return stream(clazz.getMethods())
        .filter(m -> isPublic(m.getModifiers()))
        .filter(m -> expectStatic == isStatic(m.getModifiers()))
        .collect(toList());
  }

}
