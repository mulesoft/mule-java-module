/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.internal.operation;

import static org.mule.runtime.core.api.util.ClassUtils.isInstance;
import org.mule.extensions.java.api.exception.WrongTypeModuleException;
import org.mule.extensions.java.internal.JavaModule;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.core.api.util.ClassUtils;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Utility class to common functions across operations
 *
 * @since 1.0
 */
public final class JavaModuleUtils {

  public static void validateType(String clazz, Object instance, boolean acceptSubtypes)
      throws ClassNotFoundException, WrongTypeModuleException {

    Class<?> declaredClass = ClassUtils.loadClass(clazz, JavaModule.class);

    boolean isValid = acceptSubtypes
        ? isInstance(declaredClass, instance)
        : declaredClass.equals(instance.getClass());

    if (!isValid) {
      throw new WrongTypeModuleException(clazz, instance.getClass().getName());
    }
  }

  public static List<Object> getSortedArgs(LinkedHashMap<String, TypedValue<Object>> args,
                                           Parameter[] parameters) {
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
