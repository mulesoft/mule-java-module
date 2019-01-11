/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.internal.cache;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;

import org.junit.Test;
import org.mule.extensions.java.internal.cache.JavaModuleLoadingCacheUtils;

public class JavaModuleLoadingCacheUtilsTestCase {

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
    Supplier<Long> supplier = new SupplierImpl();
    List<Method> nonBridgeMethods = JavaModuleLoadingCacheUtils.getPublicMethods(supplier.getClass(), false);
    for (Method method : nonBridgeMethods) {
      assertThat(method.isBridge(), is(false));
    }
  }
}
