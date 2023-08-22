/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.internal.util;

import java.util.function.BiFunction;

import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * Utility class for introspecting Throwables and their causes
 *
 * @since 1.2.0
 */
public class JavaExceptionUtils {

  private JavaExceptionUtils() {}

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
