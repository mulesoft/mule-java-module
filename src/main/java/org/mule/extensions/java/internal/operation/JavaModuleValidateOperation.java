/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.internal.operation;

import static org.mule.runtime.api.meta.ExpressionSupport.NOT_SUPPORTED;
import org.mule.extensions.java.api.exception.WrongTypeModuleException;
import org.mule.extensions.java.internal.JavaModule;
import org.mule.extensions.java.internal.error.JavaValidateTypeErrorProvider;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.error.Throws;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.display.ClassValue;
import org.mule.runtime.extension.api.annotation.param.stereotype.Validator;

/**
 * Defines the operations of {@link JavaModule} which executes the {@link Validator}s that the extension provides out of
 * the box
 *
 * @since 1.0
 */
public class JavaModuleValidateOperation {

  @Validator
  @Throws(JavaValidateTypeErrorProvider.class)
  public void validateType(@ClassValue @Alias("class") @Optional @Expression(NOT_SUPPORTED) String clazz,
                           Object instance,
                           @Optional(defaultValue = "true") boolean acceptSubtypes)
      throws ClassNotFoundException, WrongTypeModuleException {

    JavaModuleUtils.validateType(clazz, instance, acceptSubtypes);
  }

}
