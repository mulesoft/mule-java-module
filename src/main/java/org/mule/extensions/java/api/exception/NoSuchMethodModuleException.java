/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.api.exception;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.mule.extensions.java.api.error.JavaModuleError.NO_SUCH_METHOD;
import static org.mule.extensions.java.internal.parameters.ExecutableIdentifierFactory.create;
import org.mule.extensions.java.api.error.JavaModuleError;
import org.mule.extensions.java.internal.parameters.ExecutableIdentifier;
import org.mule.runtime.api.metadata.TypedValue;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * A {@link JavaModuleException} related with the {@link JavaModuleError#NO_SUCH_METHOD} Error type
 *
 * @since 1.0
 */
public class NoSuchMethodModuleException extends JavaModuleException {

  public NoSuchMethodModuleException(ExecutableIdentifier id, Class<?> targetClass,
                                     List<Method> methods,
                                     LinkedHashMap<String, TypedValue<Object>> args) {
    super(buildMessage(id.getElementId(), targetClass, methods, toHumanReadableArgs(args)), NO_SUCH_METHOD);
  }

  public NoSuchMethodModuleException(String id, Class<?> targetClass, List<Method> methods, List<Object> args) {
    super(buildMessage(id, targetClass, methods, toHumanReadableArgs(args)), NO_SUCH_METHOD);
  }

  private static String buildMessage(String id, Class<?> targetClass, List<Method> methods, List<String> args) {
    return format("No Method found with name [%s] in class [%s] with arguments %s. Available Methods are %s",
                  id, targetClass.getName(),
                  args,
                  methods.stream().map(c -> create(c).getElementId()).collect(toList()));
  }

}
