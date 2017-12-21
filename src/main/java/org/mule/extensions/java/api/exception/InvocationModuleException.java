/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.api.exception;

import static java.lang.String.format;
import static org.mule.extensions.java.api.error.JavaModuleError.INVOCATION;
import org.mule.extensions.java.api.error.JavaModuleError;
import org.mule.runtime.api.metadata.TypedValue;

import java.util.List;
import java.util.Map;

/**
 * A {@link JavaModuleException} related with the {@link JavaModuleError#INVOCATION} Error type
 *
 * @since 1.0
 */
public class InvocationModuleException extends JavaModuleException {

  public InvocationModuleException(String failure, Map<String, TypedValue<Object>> args, Throwable cause) {
    super(buildMessage(failure, toHumanReadableArgs(args)) + ": " + cause.getMessage(), INVOCATION, cause);
  }

  public InvocationModuleException(String failure, List<Object> args, Throwable cause) {
    super(buildMessage(failure, toHumanReadableArgs(args)) + ": " + cause.getMessage(), INVOCATION, cause);
  }

  private static String buildMessage(String failure, List<String> args) {
    return format("%s with arguments %s", failure, args);
  }

}
