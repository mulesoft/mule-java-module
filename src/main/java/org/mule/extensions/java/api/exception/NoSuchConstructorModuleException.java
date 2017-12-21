/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.api.exception;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.mule.extensions.java.api.error.JavaModuleError.NO_SUCH_CONSTRUCTOR;
import static org.mule.extensions.java.internal.parameters.ExecutableIdentifierFactory.create;
import org.mule.extensions.java.api.error.JavaModuleError;
import org.mule.extensions.java.internal.parameters.ExecutableIdentifier;
import org.mule.runtime.api.metadata.TypedValue;

import java.lang.reflect.Modifier;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link JavaModuleException} related with the {@link JavaModuleError#NO_SUCH_CONSTRUCTOR} Error type
 *
 * @since 1.0
 */
public class NoSuchConstructorModuleException extends JavaModuleException {

  private static final Logger LOGGER = LoggerFactory.getLogger(NoSuchConstructorModuleException.class);

  public NoSuchConstructorModuleException(ExecutableIdentifier id, Class<?> targetClass,
                                          Map<String, TypedValue<Object>> args) {
    super(buildMessage(id, targetClass, args), NO_SUCH_CONSTRUCTOR);
  }

  private static String buildMessage(ExecutableIdentifier id,
                                     Class<?> targetClass,
                                     Map<String, TypedValue<Object>> args) {
    String msg = format("No public Constructor found with name [%s] and arguments %s in class [%s].",
                        id.getElementId(), toHumanReadableArgs(args), targetClass.getName());

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(msg + " Available public Constructors are: " + stream(targetClass.getConstructors())
          .filter(c -> Modifier.isPublic(c.getModifiers()))
          .map(c -> create(c).getElementId()).collect(toList()));
    }

    return msg;
  }

}
