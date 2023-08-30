/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.internal.execution;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.mule.functional.api.exception.ExpectedError.none;
import org.mule.extensions.internal.JavaModuleAbstractTestCase;
import org.mule.extensions.internal.model.AnotherExecutableElement;
import org.mule.extensions.internal.model.ExecutableElement;
import org.mule.extensions.java.api.exception.InvocationModuleException;
import org.mule.functional.api.exception.ExpectedError;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.tck.junit4.rule.SystemProperty;

import org.junit.Rule;
import org.junit.Test;

public class JavaInvokeStaticOperationsTestCase extends JavaModuleAbstractTestCase {

  @Rule
  public SystemProperty className = new SystemProperty("className", ExecutableElement.class.getName());

  @Rule
  public ExpectedError expectedError = none();

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

  @Test
  public void invokeStaticThrowsExceptionWithCustomMessage() throws Exception {
    final String messageOfCause = "My internal exception message";
    final String errorMessage = "Invocation of static Method 'throwException(String)' "
        + "from Class 'org.mule.extensions.internal.model.ExecutableElement' with arguments "
        + "[java.lang.String message] resulted in an error.\n"
        + "Expected arguments are [java.lang.String message].\n"
        + "Cause: java.lang.RuntimeException - org.mule.extensions.internal.model.CustomIllegalArgumentException: "
        + messageOfCause;

    expectedError.expectError("JAVA", "INVOCATION", InvocationModuleException.class, errorMessage);
    flowRunner("invokeStaticThrowsExceptionWithCustomMessage")
        .withPayload(messageOfCause)
        .run();
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
