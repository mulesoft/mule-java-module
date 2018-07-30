/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.api.exception;

import static java.lang.reflect.Modifier.isPublic;
import static java.util.Arrays.stream;
import static org.mule.extensions.java.api.error.JavaModuleError.NO_SUCH_METHOD;
import static org.mule.extensions.java.internal.util.JavaModuleUtils.toHumanReadableArgs;
import org.mule.extensions.java.api.error.JavaModuleError;
import org.mule.extensions.java.internal.parameters.ExecutableIdentifier;
import org.mule.extensions.java.internal.parameters.ExecutableIdentifierFactory;
import org.mule.extensions.java.internal.parameters.MethodIdentifier;
import org.mule.extensions.java.internal.parameters.StaticMethodIdentifier;
import org.mule.runtime.api.metadata.TypedValue;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A {@link JavaModuleException} related with the {@link JavaModuleError#NO_SUCH_METHOD} Error type
 *
 * @since 1.0
 */
public class NoSuchMethodModuleException extends JavaModuleException {

  public NoSuchMethodModuleException(ExecutableIdentifier id, Class<?> targetClass,
                                     Map<String, TypedValue<Object>> args) {
    super(buildMessage(id, targetClass, toHumanReadableArgs(args)), NO_SUCH_METHOD);
  }

  private static String buildMessage(ExecutableIdentifier id, Class<?> targetClass, List<String> args) {
    List<String> staticMethods = new LinkedList<>();
    List<String> instanceMethods = new LinkedList<>();

    stream(targetClass.getMethods())
        .filter(m -> isPublic(m.getModifiers()))
        .map(ExecutableIdentifierFactory::create)
        .forEach(m -> {
          if (m instanceof StaticMethodIdentifier) {
            staticMethods.add(m.getElementId());
          } else {
            instanceMethods.add(m.getElementId());
          }
        });

    StringBuilder sb = new StringBuilder()
        .append("No public ").append(id.getExecutableTypeName())
        .append(" found with signature '").append(id.getElementId())
        .append("' for Class '").append(id.getClazz())
        .append("'.");

    if (!staticMethods.isEmpty()) {
      sb.append("\nPublic static Methods are ")
          .append(staticMethods);
    } else if (id instanceof StaticMethodIdentifier) {
      sb.append("\nThere are no public static Methods declared in Class ").append(id.getClazz()).append(".");
    }

    if (!instanceMethods.isEmpty()) {
      sb.append("\nPublic instance Methods are ")
          .append(instanceMethods);
    } else if (id instanceof MethodIdentifier) {
      sb.append("\nThere are no public Methods declared in Class ").append(id.getClazz()).append(".");
    }

    return sb.toString();
  }

}
