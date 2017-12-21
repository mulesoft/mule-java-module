/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.api.exception;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import org.mule.extensions.java.internal.JavaModule;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.extension.api.error.ErrorTypeDefinition;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;

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

  static List<String> toHumanReadableArgs(Map<String, TypedValue<Object>> args) {
    return args.entrySet().stream()
        .map(e -> e.getValue().getDataType().getType().getSimpleName() + " " + e.getKey())
        .collect(toList());
  }

  static List<String> toHumanReadableArgs(List<Object> args) {
    return args.stream().map(t -> t.getClass().getSimpleName()).collect(toList());
  }

  static List<String> toHumanReadableArgs(Parameter[] args) {
    return stream(args)
        .map(p -> p.getType().getSimpleName() + " " + p.getName())
        .collect(toList());
  }

}
