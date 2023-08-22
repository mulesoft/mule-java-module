/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.api.exception;

import static java.lang.reflect.Modifier.isPublic;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.mule.extensions.java.api.error.JavaModuleError.NO_SUCH_CONSTRUCTOR;
import static org.mule.extensions.java.internal.parameters.ExecutableIdentifierFactory.create;
import org.mule.extensions.java.api.error.JavaModuleError;
import org.mule.extensions.java.internal.parameters.ExecutableIdentifier;
import org.mule.runtime.api.metadata.TypedValue;

import java.util.List;
import java.util.Map;

/**
 * A {@link JavaModuleException} related with the {@link JavaModuleError#NO_SUCH_CONSTRUCTOR} Error type
 *
 * @since 1.0
 */
public class NoSuchConstructorModuleException extends JavaModuleException {

  public NoSuchConstructorModuleException(ExecutableIdentifier id, Class<?> targetClass,
                                          Map<String, TypedValue<Object>> args) {
    super(buildMessage(id, targetClass, args), NO_SUCH_CONSTRUCTOR);
  }

  private static String buildMessage(ExecutableIdentifier id, Class<?> targetClass, Map<String, TypedValue<Object>> args) {
    List<String> availableConstructors = stream(targetClass.getConstructors())
        .filter(c -> isPublic(c.getModifiers()))
        .map(c -> create(c).getElementId())
        .collect(toList());

    StringBuilder sb = new StringBuilder()
        .append("No public Constructor").append(id.getExecutableTypeName())
        .append(" found with signature '").append(id.getElementId())
        .append("' for Class '").append(id.getClazz()).append("'.");

    if (availableConstructors.isEmpty()) {
      sb.append("\nNo public Constructors are available.");
    } else {
      sb.append("\nPublic Constructors are ")
          .append(availableConstructors);
    }

    return sb.toString();
  }

}
