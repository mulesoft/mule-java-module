/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.internal.stereotypes;

import org.mule.runtime.extension.api.stereotype.MuleStereotypeDefinition;
import org.mule.runtime.extension.api.stereotype.StereotypeDefinition;

/**
 * {@link StereotypeDefinition} for a generic {@link Object} definition.
 * <p>
 * Note: duplicated class from mule-extensions-api as this stereotype is not present in the version
 * used by the connector but we won't want to increase the minMuleVersion in order to make use of it.
 * </p>
 */
public class ObjectStereotype extends MuleStereotypeDefinition {

  @Override
  public String getName() {
    return "OBJECT";
  }

}
