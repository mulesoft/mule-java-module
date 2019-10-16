/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.internal.stereotypes;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import org.mule.extensions.java.internal.stereotypes.ObjectStereotype;

import org.junit.Test;

public class ObjectStereotypeTestCase {

  @Test
  public void objectStereotypeName() {
    assertThat(new ObjectStereotype().getName(), equalTo("OBJECT"));
  }

  @Test
  public void objectStereotypeNamespace() {
    assertThat(new ObjectStereotype().getNamespace(), equalTo("MULE"));
  }

}
