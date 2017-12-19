/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java;

import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class JavaModuleAbstractTestCase extends MuleArtifactFunctionalTestCase {

  public static final String RICK = "Rick";
  public static final int RICK_ID = 137;

  @Override
  protected boolean isDisposeContextPerClass() {
    return true;
  }

  public static class Args {

    private Map<String, Object> args = new LinkedHashMap<>();

    private Args() {}

    public static Args create() {
      return new Args();
    }

    public static Args create(String name, Object value) {
      return new Args().add(name, value);
    }

    public Args add(String name, Object value) {
      args.put(name, value);
      return this;
    }

    public Map<String, Object> get() {
      return args;
    }

  }

}
