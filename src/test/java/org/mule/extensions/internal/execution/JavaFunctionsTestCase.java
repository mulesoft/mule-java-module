/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.internal.execution;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.fail;
import org.mule.extensions.internal.JavaModuleAbstractTestCase;
import org.mule.extensions.internal.model.CompositePojo;
import org.mule.extensions.internal.model.ExecutableElement;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class JavaFunctionsTestCase extends JavaModuleAbstractTestCase {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Override
  protected String getConfigFile() {
    return "java-module-functions.xml";
  }

  @Test
  public void invokeCorrect() throws Exception {
    Object value = flowRunner("invoke")
        .withPayload(new ExecutableElement())
        .withVariable("clazz", ExecutableElement.class.getName())
        .withVariable("method", "sayHi(String, int)")
        .withVariable("args", Args.create("name", "Rick").add("id", 137).get())
        .run().getMessage().getPayload().getValue();
    assertThat(value, is("Hi " + RICK + "::" + RICK_ID));
  }

  @Test
  public void invokeVoid() throws Exception {
    Object value = flowRunner("invoke")
        .withPayload(new ExecutableElement())
        .withVariable("clazz", ExecutableElement.class.getName())
        .withVariable("method", "nextPhase()")
        .run().getMessage().getPayload().getValue();
    assertThat(value, is(nullValue()));
  }

  @Test
  public void invokeInlineInput() throws Exception {
    CompositePojo payload = new CompositePojo("inlined");
    flowRunner("invokeInlineMap")
        .withPayload(payload)
        .run();

    assertThat(payload.getChilds().get("repeat"), is(not(nullValue())));
    assertThat(payload.getChilds().get("repeat").size(), is(2));
    assertThat(payload.getChilds().get("repeat").get(0).getName(), is("inlined"));
  }

  @Test
  public void isInstanceOfCorrect() throws Exception {
    Boolean value = (Boolean) flowRunner("isInstanceOf")
        .withPayload(new ExecutableElement())
        .withVariable("clazz", ExecutableElement.class.getName())
        .run().getMessage().getPayload().getValue();
    assertThat(value, is(true));
  }

  @Test
  public void invokeNoSuchMethodModuleException() throws Exception {
    String className = ExecutableElement.class.getName();
    String methodName = "missingMethod";
    try {
      flowRunner("invoke")
          .withPayload(new ExecutableElement())
          .withVariable("clazz", className)
          .withVariable("method", methodName)
          .run();

      fail("NO_SUCH_METHOD exception was expected");
    } catch (Exception e) {
      assertThat(e.getMessage(), containsString("No public Method found with signature 'missingMethod'"));
      assertThat(e.getMessage(), containsString("for Class '" + className));
      assertThat(e.getMessage(), containsString("Public static Methods are ["));
      assertThat(e.getMessage(), containsString("Public instance Methods are ["));
    }
  }

  @Test
  public void isInstanceOfFalse() throws Exception {
    Boolean value = (Boolean) flowRunner("isInstanceOf")
        .withPayload(new ExecutableElement())
        .withVariable("clazz", Integer.class.getName())
        .run().getMessage().getPayload().getValue();
    assertThat(value, is(false));
  }

  @Test
  public void isInstanceOfWrongClass() throws Exception {
    // Different runtime version provide different messages
    expectedException.expectMessage(anyOf(containsString("Cannot load class 'zarazarasa'"),
                                          containsString("Couldn't find class: zarazarasa")));

    Boolean value = (Boolean) flowRunner("isInstanceOf")
        .withPayload(new ExecutableElement())
        .withVariable("clazz", "zarazarasa")
        .run().getMessage().getPayload().getValue();
    assertThat(value, is(true));
  }

}
