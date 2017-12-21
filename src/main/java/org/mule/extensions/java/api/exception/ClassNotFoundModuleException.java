/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.api.exception;

import static org.mule.extensions.java.api.error.JavaModuleError.CLASS_NOT_FOUND;
import org.mule.extensions.java.api.error.JavaModuleError;

/**
 * A {@link JavaModuleException} related with the {@link JavaModuleError#NOT_INSTANTIABLE_TYPE} Error type
 *
 * @since 1.0
 */
public class ClassNotFoundModuleException extends JavaModuleException {

  public ClassNotFoundModuleException(String message) {
    super(message, CLASS_NOT_FOUND);
  }

  public ClassNotFoundModuleException(String message, Throwable cause) {
    super(message, CLASS_NOT_FOUND, cause);
  }

}
