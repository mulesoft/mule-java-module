/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.internal.util;

import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.api.exception.TypedException;
import org.mule.runtime.api.message.Error;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.lang.reflect.InvocationTargetException;
import java.util.function.BiFunction;

import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * Utility class for introspecting Mule Errors and their Java causes
 *
 * @since 1.2.0
 */
public class JavaErrorUtils {

  private JavaErrorUtils() {}

  public static Throwable getCause(Error error) {
    Throwable actualCause = error.getCause();
    while (isMuleExceptionWrapper(actualCause)) {
      actualCause = actualCause.getCause();
    }
    if (actualCause instanceof InvocationTargetException) {
      actualCause = ((InvocationTargetException) actualCause).getTargetException();
    }

    return actualCause != null ? actualCause : error.getCause();
  }

  public static Throwable getRootCause(Error error) {
    Throwable rootCause = ExceptionUtils.getRootCause(error.getCause());
    return rootCause != null ? rootCause : error.getCause();
  }

  public static boolean isCausedBy(Error error, Class<?> typeOfCause, boolean includeSubtypes) {
    BiFunction<Throwable, Class, Integer> resolver = includeSubtypes
        ? ExceptionUtils::indexOfType
        : ExceptionUtils::indexOfThrowable;

    return resolver.apply(error.getCause(), typeOfCause) > -1;
  }

  private static boolean isMuleExceptionWrapper(Throwable exception) {
    return exception instanceof ModuleException
        || exception instanceof TypedException
        || exception instanceof MuleRuntimeException;
  }
}
