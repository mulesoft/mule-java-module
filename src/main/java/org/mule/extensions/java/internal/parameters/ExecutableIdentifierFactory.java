/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.internal.parameters;

import static java.lang.reflect.Modifier.isStatic;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;

/**
 * Factory implementation for obtaining an {@link ExecutableIdentifier}
 * from a given {@link Executable} element
 *
 * @since 1.0
 */
public final class ExecutableIdentifierFactory {

  private ExecutableIdentifierFactory() {}

  /**
   * @param executable the {@link Executable} to represent
   * @return a new instance of {@link ExecutableIdentifier} that identifies the {@code executable}
   */
  public static ExecutableIdentifier create(Executable executable) {
    if (executable instanceof Constructor) {
      return new ConstructorIdentifier((Constructor) executable);
    }

    return isStatic(executable.getModifiers())
        ? new StaticMethodIdentifier((Method) executable)
        : new MethodIdentifier((Method) executable);
  }

}
