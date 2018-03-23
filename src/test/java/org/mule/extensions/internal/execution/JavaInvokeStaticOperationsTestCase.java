/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.internal.execution;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;

import org.mule.extensions.internal.model.AnotherExecutableElement;
import org.mule.extensions.internal.model.ExecutableElement;
import org.mule.extensions.internal.JavaModuleAbstractTestCase;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.tck.junit4.rule.SystemProperty;

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.junit.Rule;
import org.junit.Test;

public class JavaInvokeStaticOperationsTestCase extends JavaModuleAbstractTestCase {

  @Rule
  public SystemProperty className = new SystemProperty("className", ExecutableElement.class.getName());

  @Override
  protected String getConfigFile() {
    return "java-invoke-operation.xml";
  }

  @Test
  public void invokeStaticOverloadNoArgs() throws Exception {
    TypedValue<ExecutableElement> payload = invokeStatic("create()");
    assertThat(payload.getValue(), is(instanceOf(ExecutableElement.class)));
    assertThat(payload.getValue().getPhase(), is(ExecutableElement.Phase.NOT_STARTED));
  }

  @Test
  public void invokeStaticOverloadWithArgs() throws Exception {
    TypedValue<ExecutableElement> payload = invokeStatic("create(Phase)",
                                                         Args.create("initPhase", ExecutableElement.Phase.STOPPED));
    assertThat(payload.getValue(), is(instanceOf(ExecutableElement.class)));
    assertThat(payload.getValue().getPhase(), is(ExecutableElement.Phase.STOPPED));
  }

  @Test
  public void invokeStaticWithTwoDifferentClassesWithSameMethodSignature() throws Exception {
    TypedValue<String> payload = flowRunner("invokeStaticWithTwoDifferentClassesWithSameMethodSignature")
        .run()
        .getMessage()
        .getPayload();
    assertThat(payload.getValue(), is(AnotherExecutableElement.className()));
  }

  private <T> TypedValue<T> invokeStatic(String method) throws Exception {
    return flowRunner("invokeStaticNoArgs")
        .withVariable("method", method)
        .run()
        .getMessage()
        .getPayload();
  }

  private <T> TypedValue<T> invokeStatic(String method, Args args)
      throws Exception {
    return flowRunner("invokeStaticWithArgs")
        .withVariable("method", method)
        .withVariable("args", args.get())
        .run()
        .getMessage()
        .getPayload();
  }

}
