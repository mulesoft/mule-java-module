/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.internal.parameters;

import static org.mule.runtime.api.meta.ExpressionSupport.SUPPORTED;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.metadata.MetadataKeyPart;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.ClassValue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;

/**
 * //TODO
 */
public class ConstructorIdentifier extends ExecutableIdentifier {

  @Parameter
  @Alias("class")
  @ClassValue
  @Expression(SUPPORTED)
  @MetadataKeyPart(order = 1, providedByKeyResolver = false)
  private String clazz;

  @Parameter
  @MetadataKeyPart(order = 2)
  @Alias("constructor")
  private String constructorId;

  public ConstructorIdentifier() {}

  ConstructorIdentifier(Constructor constructor) {
    clazz = constructor.getDeclaringClass().getName();
    constructorId = buildId(constructor.getDeclaringClass().getSimpleName(), constructor.getParameterTypes());
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
    return element instanceof Constructor && new ConstructorIdentifier((Constructor) element).equals(this);
  }

}
