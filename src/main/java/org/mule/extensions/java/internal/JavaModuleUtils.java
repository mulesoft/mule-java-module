/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.internal;

import static org.mule.runtime.core.api.util.ClassUtils.isInstance;
import org.mule.extensions.java.api.exception.ArgumentMismatchModuleException;
import org.mule.extensions.java.api.exception.ClassNotFoundModuleException;
import org.mule.extensions.java.api.exception.InvocationModuleException;
import org.mule.extensions.java.api.exception.NoSuchMethodModuleException;
import org.mule.extensions.java.api.exception.WrongTypeModuleException;
import org.mule.extensions.java.internal.cache.JavaModuleLoadingCache;
import org.mule.runtime.api.metadata.TypedValue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Utility class to common functions across operations
 *
 * @since 1.0
 */
public final class JavaModuleUtils {

  private JavaModuleUtils() {}

  public static void validateType(String clazz, Object instance, boolean acceptSubtypes, JavaModuleLoadingCache cache)
      throws ClassNotFoundModuleException, WrongTypeModuleException {

    Class<?> declaredClass = cache.loadClass(clazz);

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

    try {
      List<Object> sortedArgs = JavaModuleUtils.getSortedArgs(args, method.getParameters());
      if (sortedArgs.size() == method.getParameters().length) {
        return method.invoke(instance, sortedArgs.toArray());
      }

      throw new ArgumentMismatchModuleException(failureMessageProvider.get(), method, args);
    } catch (IllegalArgumentException e) {
      throw new ArgumentMismatchModuleException(failureMessageProvider.get(), method, args, e);

    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new InvocationModuleException(failureMessageProvider.get(), args, e);
    }
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

}
