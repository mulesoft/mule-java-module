/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.api.exception;

import static org.mule.extensions.java.api.error.JavaModuleError.WRONG_INSTANCE_CLASS;

import org.mule.extensions.java.api.error.JavaModuleError;

/**
 * A {@link JavaModuleException} related with the {@link JavaModuleError#WRONG_INSTANCE_CLASS} Error type
 *
 * @since 1.0
 */
public class WrongTypeModuleException extends JavaModuleException {

  public WrongTypeModuleException(String expected, String actual) {
    super("Expected an instance of type [%s] but was [%s]".formatted(expected, actual), WRONG_INSTANCE_CLASS);
  }

}
