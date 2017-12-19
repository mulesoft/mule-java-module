/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.api.exception;

import static java.lang.String.format;
import static org.mule.extensions.java.api.error.JavaModuleError.ARGUMENTS_MISMATCH;
import org.mule.extensions.java.api.error.JavaModuleError;
import org.mule.runtime.api.metadata.TypedValue;

import java.lang.reflect.Executable;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * A {@link JavaModuleException} related with the {@link JavaModuleError#ARGUMENTS_MISMATCH} Error type
 *
 * @since 1.0
 */
public class ArgumentMismatchModuleException extends JavaModuleException {

  public ArgumentMismatchModuleException(String failure, Executable executable, LinkedHashMap<String, TypedValue<Object>> args) {
    super(buildMessage(failure, executable, toHumanReadableArgs(args)), ARGUMENTS_MISMATCH);
  }

  public ArgumentMismatchModuleException(String failure, Executable executable, List<Object> args, Throwable cause) {
    super(buildMessage(failure, executable, toHumanReadableArgs(args)) + ": " + cause.getMessage(),
          ARGUMENTS_MISMATCH, cause);
  }

  public ArgumentMismatchModuleException(String failure, Executable executable,
                                         LinkedHashMap<String, TypedValue<Object>> args, Throwable cause) {
    super(buildMessage(failure, executable, toHumanReadableArgs(args)) + ": " + cause.getMessage(),
          ARGUMENTS_MISMATCH, cause);
  }

  private static String buildMessage(String failure, Executable executable, List<String> args) {

    return format("%s with arguments %s. Expected arguments are %s", failure,
                  args, toHumanReadableArgs(executable.getParameters()));
  }

}
