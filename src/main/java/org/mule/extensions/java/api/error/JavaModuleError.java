/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.api.error;

import org.mule.extensions.java.internal.JavaModule;
import org.mule.runtime.extension.api.error.ErrorTypeDefinition;
import org.mule.runtime.extension.api.error.MuleErrors;

import java.util.Optional;

/**
 * The {@link ErrorTypeDefinition}s for the {@link JavaModule}
 * 
 * @since 1.0
 */
public enum JavaModuleError implements ErrorTypeDefinition<JavaModuleError> {

  /**
   * No definition for the class with the specified name could be found
   */
  CLASS_NOT_FOUND,

  /**
   * If the supplied class is abstract or an interface
   */
  NOT_INSTANTIABLE_TYPE,

  /**
   * The class doesn't have a matching constructor or it is not visible
   */
  NO_SUCH_CONSTRUCTOR,

  /**
   * Any of the arguments is of the wrong type, missing or too many arguments were provided
   */
  ARGUMENTS_MISMATCH,

  /**
   * An error occurred during the invocation of a Method or Constructor
   */
  INVOCATION,

  /**
   * The specified method cannot be found
   */
  NO_SUCH_METHOD,

  /**
   * The Class of the a given instance does not match with the expected Class
   */
  WRONG_INSTANCE_CLASS(MuleErrors.VALIDATION);

  private ErrorTypeDefinition parent;

  JavaModuleError() {}

  JavaModuleError(ErrorTypeDefinition<?> parent) {
    this.parent = parent;
  }

  @Override
  public Optional<ErrorTypeDefinition<? extends Enum<?>>> getParent() {
    return Optional.ofNullable(parent);
  }
}
