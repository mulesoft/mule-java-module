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
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link JavaModuleException} related with the {@link JavaModuleError#NO_SUCH_METHOD} Error type
 *
 * @since 1.0
 */
public class NoSuchMethodModuleException extends JavaModuleException {

  private static final Logger LOGGER = LoggerFactory.getLogger(NoSuchMethodModuleException.class);

  public NoSuchMethodModuleException(ExecutableIdentifier id, Class<?> targetClass,
                                     List<Method> methods,
                                     Map<String, TypedValue<Object>> args) {
    super(buildMessage(id.getElementId(), targetClass, methods, toHumanReadableArgs(args)), NO_SUCH_METHOD);
  }

  public NoSuchMethodModuleException(String id, Class<?> targetClass, List<Method> methods, List<Object> args) {
    super(buildMessage(id, targetClass, methods, toHumanReadableArgs(args)), NO_SUCH_METHOD);
  }

  private static String buildMessage(String id, Class<?> targetClass, List<Method> methods, List<String> args) {
    String msg = format("No public Method found with name [%s] in class [%s] with arguments %s.",
                        id, targetClass.getName(), args);

    if (LOGGER.isDebugEnabled()) {
      LOGGER
          .debug(msg + " Available public Methods are: " + methods.stream().map(c -> create(c).getElementId()).collect(toList()));
    }

    return msg;
  }

}
