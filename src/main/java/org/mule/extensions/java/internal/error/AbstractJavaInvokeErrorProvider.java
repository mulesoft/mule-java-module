/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.internal.error;

import org.mule.extensions.java.api.error.JavaModuleError;
import org.mule.runtime.extension.api.annotation.error.ErrorTypeProvider;
import org.mule.runtime.extension.api.error.ErrorTypeDefinition;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * An abstract {@link ErrorTypeProvider} used for instance and static invocations of Java methods.
 *
 * @since 1.1.2 1.2.0
 */
public abstract class AbstractJavaInvokeErrorProvider implements ErrorTypeProvider {

  @Override
  public Set<ErrorTypeDefinition> getErrorTypes() {
    Set<ErrorTypeDefinition> errors = new LinkedHashSet<>();
    errors.add(JavaModuleError.INVOCATION);
    errors.add(JavaModuleError.ARGUMENTS_MISMATCH);
    errors.add(JavaModuleError.NO_SUCH_METHOD);
    errors.add(JavaModuleError.CLASS_NOT_FOUND);

    return errors;
  }
}

