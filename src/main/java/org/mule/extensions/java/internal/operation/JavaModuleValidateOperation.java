/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.internal.operation;

import static org.mule.runtime.api.meta.ExpressionSupport.NOT_SUPPORTED;

import org.mule.extensions.java.api.exception.ClassNotFoundModuleException;
import org.mule.extensions.java.api.exception.WrongTypeModuleException;
import org.mule.extensions.java.internal.JavaModule;
import org.mule.extensions.java.internal.util.JavaModuleUtils;
import org.mule.extensions.java.internal.cache.JavaModuleLoadingCache;
import org.mule.extensions.java.internal.error.JavaValidateTypeErrorProvider;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.error.Throws;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.display.ClassValue;
import org.mule.runtime.extension.api.annotation.param.stereotype.Validator;

import javax.inject.Inject;

/**
 * Defines the operations of {@link JavaModule} which executes the {@link Validator}s that the extension provides out of
 * the box
 *
 * @since 1.0
 */
public class JavaModuleValidateOperation {

  @Inject
  private JavaModuleLoadingCache cache;

  /**
   * Operation that allows the user to validate that a given {@code instance} is an {@code instanceof} the specified {@code class}.
   *
   * @param clazz the fully qualified name of the expected {@link Class} for the instance
   * @param instance the object whose type is expected to be an {@code instanceof} of the given {@code class}
   * @param acceptSubtypes whether or not to accept sub types of the given {@code class} or if the instance has to be
   *                       of the exact same {@code class}
   * @throws ClassNotFoundModuleException if the given {@code class} is not found in the current context
   * @throws WrongTypeModuleException if the validation fails because the {@code instance} is not of the expected {@code class} type
   */
  @Validator
  @Throws(JavaValidateTypeErrorProvider.class)
  public void validateType(@ClassValue @Alias("class") @Optional @Expression(NOT_SUPPORTED) String clazz,
                           Object instance,
                           @Optional(defaultValue = "true") boolean acceptSubtypes)
      throws ClassNotFoundModuleException, WrongTypeModuleException {

    JavaModuleUtils.validateType(clazz, instance, acceptSubtypes, cache);
  }

}
