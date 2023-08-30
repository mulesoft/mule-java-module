/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.internal.model.real;

public class Cat {

  private String name;

  public Cat() {}

  public Cat(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public String saySomething() {
    return "Meow";
  }

}
