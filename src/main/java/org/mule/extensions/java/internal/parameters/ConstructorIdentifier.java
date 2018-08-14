/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.internal.parameters;

import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.metadata.MetadataKeyPart;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.ClassValue;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;

/**
 * A unique identifier for a given {@link Constructor}
 *
 * @since 1.0
 */
public class ConstructorIdentifier extends ExecutableIdentifier {

  public static final String NAME = "Constructor";

  /**
   * Represents the fully qualified name of the Class containing the referenced Method.
   */
  @Parameter
  @Alias("class")
  @ClassValue
  @MetadataKeyPart(order = 1, providedByKeyResolver = false)
  @Summary("Fully qualified name of the Class containing the referenced Method")
  private String clazz;

  /**
   * Represents the Constructor signature containing the name and it's argument types.
   * <p>
   * For example, for the Constructor with signature {@code public Foo(String name, Integer age)}
   * then the identifier of the method will be {@code "Foo(String, Integer)"}
   */
  @Parameter
  @Alias("constructor")
  @MetadataKeyPart(order = 2)
  @Summary("Represents the Constructor signature containing the name and it's argument types.")
  private String constructorId;

  public ConstructorIdentifier() {}

  public ConstructorIdentifier(Constructor constructor) {
    this(constructor.getDeclaringClass().getName(),
         constructor.getDeclaringClass().getSimpleName(), constructor.getParameterTypes());
  }

  public ConstructorIdentifier(String className, String constructor, Class<?>[] argTypes) {
    clazz = className;
    constructorId = buildId(constructor, argTypes);
  }

  @Override
  public String getClazz() {
    return clazz;
  }

  @Override
  public String getElementId() {
    return constructorId;
  }

  @Override
  public boolean matches(Executable element) {
    return element instanceof Constructor
        && matchesArguments(element.getParameterTypes());
  }

  @Override
  public String getExecutableTypeName() {
    return NAME;
  }

}
