/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.internal.error;

import org.mule.extensions.java.api.error.JavaModuleError;
import org.mule.extensions.java.internal.operation.JavaNewInstanceOperation;
import org.mule.runtime.extension.api.annotation.error.ErrorTypeProvider;
import org.mule.runtime.extension.api.error.ErrorTypeDefinition;

import com.google.common.collect.ImmutableSet;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * An {@link ErrorTypeProvider} for the {@link JavaNewInstanceOperation#newInstance} operation.
 *
 * @since 1.0
 */
public class JavaNewInstanceErrorProvider implements ErrorTypeProvider {

  @Override
  public Set<ErrorTypeDefinition> getErrorTypes() {
    Set<ErrorTypeDefinition> errors = new LinkedHashSet<>();
    errors.add(JavaModuleError.NO_SUCH_CONSTRUCTOR);
    errors.add(JavaModuleError.CLASS_NOT_FOUND);
    errors.add(JavaModuleError.ARGUMENTS_MISMATCH);
    errors.add(JavaModuleError.NOT_INSTANTIABLE_TYPE);

    return Collections.unmodifiableSet(errors);
  }
}
