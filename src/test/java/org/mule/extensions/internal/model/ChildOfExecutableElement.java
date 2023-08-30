/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.internal.model;

public class ChildOfExecutableElement extends ExecutableElement {

  public static ChildOfExecutableElement create() {
    return new ChildOfExecutableElement();
  }

  @Override
  public String sayHi() {
    return "ChildHi";
  }

}
