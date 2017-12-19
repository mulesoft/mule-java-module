/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.api.exception;

import static java.lang.String.format;
import static org.mule.extensions.java.api.error.JavaModuleError.NOT_INSTANTIABLE_TYPE;
import org.mule.extensions.java.api.error.JavaModuleError;
import org.mule.extensions.java.internal.parameters.ExecutableIdentifier;
import org.mule.runtime.api.metadata.TypedValue;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * A {@link JavaModuleException} related with the {@link JavaModuleError#NOT_INSTANTIABLE_TYPE} Error type
 *
 * @since 1.0
 */
public class NonInstantiableTypeModuleException extends JavaModuleException {

  public NonInstantiableTypeModuleException(ExecutableIdentifier id,
                                            LinkedHashMap<String, TypedValue<Object>> args,
                                            Throwable cause) {
    super(buildMessage(id.getClazz(), toHumanReadableArgs(args)) + ": " + cause.getMessage(), NOT_INSTANTIABLE_TYPE, cause);
  }

  private static String buildMessage(String className, List<String> args) {
    return format("Failed to instantiate class [%s] with parameters %s", className, args);
  }

}
