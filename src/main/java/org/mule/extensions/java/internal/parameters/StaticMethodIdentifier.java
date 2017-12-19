/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.internal.parameters;

import static java.lang.reflect.Modifier.isStatic;
import static org.mule.runtime.api.meta.ExpressionSupport.NOT_SUPPORTED;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.metadata.MetadataKeyPart;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.ClassValue;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;

/**
 * A unique identifier for a given static {@link Method}
 *
 * @since 1.0
 */
public class StaticMethodIdentifier extends ExecutableIdentifier {

  @Parameter
  @Alias("class")
  @ClassValue
  @Expression(NOT_SUPPORTED)
  @MetadataKeyPart(order = 1, providedByKeyResolver = false)
  private String clazz;

  @Parameter
  @MetadataKeyPart(order = 2)
  @Alias("method")
  private String methodId;

  public StaticMethodIdentifier() {}

  StaticMethodIdentifier(Method method) {
    clazz = method.getDeclaringClass().getName();
    methodId = buildId(method.getName(), method.getParameterTypes());
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
        && this.equals(new StaticMethodIdentifier((Method) element));
  }

}
