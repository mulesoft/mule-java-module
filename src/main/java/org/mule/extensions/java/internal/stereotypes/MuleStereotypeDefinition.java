/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.internal.stereotypes;

import org.mule.api.annotation.NoExtend;
import org.mule.runtime.extension.api.stereotype.StereotypeDefinition;

/**
 * Marker interface that identifies a given {@link StereotypeDefinition} as a Mule stereotype.
 */
@NoExtend
public abstract class MuleStereotypeDefinition implements StereotypeDefinition {

  public static final String NAMESPACE = "MULE";

  @Override
  public final String getNamespace() {
    return NAMESPACE;
  }
}
