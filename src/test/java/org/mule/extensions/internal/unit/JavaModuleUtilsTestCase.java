/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.internal.unit;

import static org.mule.extensions.java.internal.util.JavaModuleUtils.getPublicMethods;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.Test;

public class JavaModuleUtilsTestCase {

  interface Supplier<T> {

    public T get(String string);
  }

  public class SupplierImpl implements Supplier<Long> {

    @Override
    public Long get(String string) {
      return 10L;
    }

  }

  @Test
  public void getPublicMethodsReturnsNonBridgeMethods() {
    SupplierImpl supplier = new SupplierImpl();
    List<Method> nonBridgeMethods = getPublicMethods(supplier.getClass(), false);
    for (Method method : nonBridgeMethods) {
      assertThat(method.isBridge(), is(false));
    }
  }
}
