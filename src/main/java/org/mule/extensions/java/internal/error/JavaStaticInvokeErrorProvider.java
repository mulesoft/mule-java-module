/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.internal.error;

import org.mule.runtime.extension.api.annotation.error.ErrorTypeProvider;
import org.mule.runtime.extension.api.error.ErrorTypeDefinition;

import java.util.Collections;
import java.util.Set;

/**
 * An {@link ErrorTypeProvider} for the Java static invoke operation.
 *
 * @since 1.1.2 1.2.0
 */
public class JavaStaticInvokeErrorProvider extends AbstractJavaInvokeErrorProvider {

  @Override
  public Set<ErrorTypeDefinition> getErrorTypes() {
    return Collections.unmodifiableSet(super.getErrorTypes());
  }
}
