/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.internal.util;

import java.util.function.BiFunction;

import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * Utility class for introspecting Mule Errors and their Java causes
 *
 * @since 1.2.0
 */
public class JavaErrorUtils {

  private JavaErrorUtils() {}

  public static Throwable getRootCause(Throwable exception) {
    Throwable rootCause = ExceptionUtils.getRootCause(exception);
    return rootCause != null ? rootCause : exception;
  }

  public static boolean isCausedBy(Throwable exception, Class<?> typeOfCause, boolean includeSubtypes) {
    BiFunction<Throwable, Class, Integer> resolver = includeSubtypes
        ? ExceptionUtils::indexOfType
        : ExceptionUtils::indexOfThrowable;

    return resolver.apply(exception, typeOfCause) > -1;
  }

}
