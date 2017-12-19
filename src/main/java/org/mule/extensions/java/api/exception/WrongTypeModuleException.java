/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.api.exception;

import static java.lang.String.format;
import static org.mule.extensions.java.api.error.JavaModuleError.WRONG_INSTANCE_CLASS;
import org.mule.extensions.java.api.error.JavaModuleError;

/**
 * A {@link JavaModuleException} related with the {@link JavaModuleError#WRONG_INSTANCE_CLASS} Error type
 *
 * @since 1.0
 */
public class WrongTypeModuleException extends JavaModuleException {

  public WrongTypeModuleException(String expected, String actual) {
    super(format("Expected an instance of type [%s] but was [%s]", expected, actual), WRONG_INSTANCE_CLASS);
  }

}
