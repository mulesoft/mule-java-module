/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.internal.cache;

import java.lang.reflect.Method;
import java.util.List;

import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

public final class JavaModuleLoadingCacheUtils {

  private JavaModuleLoadingCacheUtils() {

  }

  public static List<Method> getPublicMethods(Class<?> clazz, boolean expectStatic) {
    return stream(clazz.getMethods())
        .filter(m -> isPublic(m.getModifiers()))
        .filter(m -> expectStatic == isStatic(m.getModifiers()))
        .filter(m -> !m.isBridge())
        .collect(toList());
  }
}
