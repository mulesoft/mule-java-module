/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.api.exception;

import org.mule.extensions.java.api.error.JavaModuleError;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.extension.api.runtime.exception.ExceptionHandler;

/**
 * //TODO
 */
public class JavaModuleExceptionHandler extends ExceptionHandler {

  @Override
  public Exception enrichException(Exception e) {
    if (e instanceof ClassNotFoundException) {
      return new ModuleException(JavaModuleError.CLASS_NOT_FOUND, e);
    }

    if (e instanceof IllegalAccessException) {
      return new ModuleException(JavaModuleError.INVOCATION, e);
    }

    return e;
  }
}
