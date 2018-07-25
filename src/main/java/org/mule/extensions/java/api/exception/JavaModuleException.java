/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.api.exception;

import org.mule.extensions.java.internal.JavaModule;
import org.mule.runtime.extension.api.error.ErrorTypeDefinition;
import org.mule.runtime.extension.api.exception.ModuleException;

/**
 * Base {@link ModuleException} for all the Exceptions thrown by the {@link JavaModule} namespace
 *
 * @since 1.0
 */
public abstract class JavaModuleException extends ModuleException {


  <T extends Enum<T>> JavaModuleException(String message,
                                          ErrorTypeDefinition<T> errorTypeDefinition) {
    super(message, errorTypeDefinition);
  }

  <T extends Enum<T>> JavaModuleException(String message,
                                          ErrorTypeDefinition<T> errorTypeDefinition, Throwable cause) {
    super(message, errorTypeDefinition, cause);
  }

}
