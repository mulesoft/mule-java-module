/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.internal.error;

import org.mule.extensions.java.api.error.JavaModuleError;
import org.mule.extensions.java.internal.operation.JavaModuleValidateOperation;
import org.mule.runtime.extension.api.annotation.error.ErrorTypeProvider;
import org.mule.runtime.extension.api.error.ErrorTypeDefinition;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * An {@link ErrorTypeProvider} for the {@link JavaModuleValidateOperation#validateType} operation.
 *
 * @since 1.0
 */
public class JavaValidateTypeErrorProvider implements ErrorTypeProvider {

  @Override
  public Set<ErrorTypeDefinition> getErrorTypes() {
    Set<ErrorTypeDefinition> errors = new LinkedHashSet<>();
    errors.add(JavaModuleError.CLASS_NOT_FOUND);
    errors.add(JavaModuleError.WRONG_INSTANCE_CLASS);

    return Collections.unmodifiableSet(errors);
  }
}
