/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.internal.parameters;

import static org.mule.runtime.api.meta.ExpressionSupport.NOT_SUPPORTED;

import static java.lang.reflect.Modifier.isStatic;

import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.metadata.MetadataKeyPart;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.ClassValue;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;

/**
 * A unique identifier for a given static {@link Method}
 *
 * @since 1.0
 */
public class StaticMethodIdentifier extends ExecutableIdentifier {

  public static final String NAME = "static Method";

  /**
   * Represents the fully qualified name of the Class containing the referenced Method.
   */
  @Parameter
  @Alias("class")
  @ClassValue
  @Expression(NOT_SUPPORTED)
  @MetadataKeyPart(order = 1, providedByKeyResolver = false)
  @Summary("Fully qualified name of the Class containing the referenced Method")
  private String clazz;

  /**
   * Represents the Method signature containing the method name and it's argument types.
   * <p>
   * For example, for the method with signature {@code public static String log(String msg, boolean verbose)} then the identifier
   * of the method will be {@code "log(String, boolean)"}
   */
  @Parameter
  @MetadataKeyPart(order = 2)
  @Alias("method")
  @Summary("Represents the Method signature containing the method name and it's argument types.")
  private String methodId;

  public StaticMethodIdentifier() {}

  public StaticMethodIdentifier(Method method) {
    this(method.getDeclaringClass().getName(), method.getName(), method.getParameterTypes());
  }

  public StaticMethodIdentifier(String className, String methodName, Class<?>[] argTypes) {
    clazz = className;
    methodId = buildId(methodName, argTypes);
  }

  @Override
  public String getClazz() {
    return clazz;
  }

  @Override
  public String getElementId() {
    return methodId;
  }

  @Override
  public boolean matches(Executable element) {
    return element instanceof Method
        && isStatic(element.getModifiers())
        && getElementName().equals(element.getName())
        && matchesArguments(element.getParameterTypes());
  }

  @Override
  public String getExecutableTypeName() {
    return NAME;
  }

}
