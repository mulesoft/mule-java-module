/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.internal.execution;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import org.mule.extensions.internal.model.CompositePojo;
import org.mule.extensions.internal.model.ExecutableElement;
import org.mule.extensions.internal.JavaModuleAbstractTestCase;
import org.mule.runtime.api.metadata.TypedValue;

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.junit.Test;

public class JavaNewInstanceOperationsTestCase extends JavaModuleAbstractTestCase {

  @Override
  protected String getConfigFile() {
    return "java-new-instance-operation.xml";
  }

  @Test
  public void newInstanceNoArgs() throws Exception {
    TypedValue<ExecutableElement> element = flowRunner("newInstanceNoArgs")
        .withVariable("class", ExecutableElement.class.getName())
        .withVariable("constructor", "ExecutableElement()")
        .run().getMessage().getPayload();
    assertThat(element.getValue(), is(instanceOf(ExecutableElement.class)));
  }

  @Test
  public void newInstanceWithArgs() throws Exception {
    TypedValue<ExecutableElement> element = flowRunner("newInstanceWithArgs")
        .withVariable("class", ExecutableElement.class.getName())
        .withVariable("constructor", "ExecutableElement(Phase)")
        .withVariable("args", Args.create("initPhase", ExecutableElement.Phase.STOPPED).get())
        .run().getMessage().getPayload();
    assertThat(element.getValue(), is(instanceOf(ExecutableElement.class)));
    MatcherAssert.assertThat(element.getValue().getPhase(), Is.is(ExecutableElement.Phase.STOPPED));
  }

  @Test
  public void newInstanceWithConstructorWithArgsById() throws Exception {
    CompositePojo rick = newInstance(CompositePojo.class.getName(),
                                     "CompositePojo(String)",
                                     Args.create("name", RICK));
    assertThat(rick, is(instanceOf(CompositePojo.class)));
    assertThat(rick.getName(), is(RICK));

    CompositePojo parentOfRick = newInstance(CompositePojo.class.getName(),
                                             "CompositePojo(Map)",
                                             Args.create("childs", singletonMap("rick", singletonList(rick))));
    assertThat(parentOfRick, is(instanceOf(CompositePojo.class)));
    assertThat(parentOfRick.getChilds().get("rick").get(0).getName(), is(RICK));
    parentOfRick.setName("parentOfRick");

    CompositePojo grandParentOfRick = newInstance(CompositePojo.class.getName(),
                                                  "CompositePojo(CompositePojo)",
                                                  Args.create("child", parentOfRick));

    assertThat(grandParentOfRick, is(instanceOf(CompositePojo.class)));
    assertThat(grandParentOfRick.getChilds().get("parentOfRick"), is(notNullValue()));
  }

  private <T> T newInstance(String clazz, String constructor, Args args) throws Exception {
    return (T) flowRunner("newInstanceWithConstructorWithArgs")
        .withVariable("class", clazz)
        .withVariable("constructor", constructor)
        .withVariable("args", args.get())
        .run().getMessage().getPayload().getValue();
  }

}
