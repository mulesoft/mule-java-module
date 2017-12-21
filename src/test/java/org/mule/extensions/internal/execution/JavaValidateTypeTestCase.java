/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.internal.execution;

import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mule.functional.api.exception.ExpectedError.none;
import org.mule.extensions.internal.model.ChildOfExecutableElement;
import org.mule.extensions.internal.model.CompositePojo;
import org.mule.extensions.internal.model.ExecutableElement;
import org.mule.extensions.internal.JavaModuleAbstractTestCase;
import org.mule.extensions.java.api.exception.WrongTypeModuleException;
import org.mule.functional.api.exception.ExpectedError;
import org.mule.tck.junit4.rule.SystemProperty;

import org.junit.Rule;
import org.junit.Test;

public class JavaValidateTypeTestCase extends JavaModuleAbstractTestCase {

  @Rule
  public SystemProperty className = new SystemProperty("className", ExecutableElement.class.getName());

  @Rule
  public ExpectedError expectedError = none();

  @Override
  protected String getConfigFile() {
    return "java-validate-operation.xml";
  }

  @Test
  public void validateExactTypeFails() throws Exception {
    expectedError.expectError("JAVA", "WRONG_INSTANCE_CLASS", WrongTypeModuleException.class,
                              format("Expected an instance of type [%s] but was [%s]",
                                     ExecutableElement.class.getName(), ChildOfExecutableElement.class.getName()));

    flowRunner("validateType")
        .withVariable("instance", new ChildOfExecutableElement())
        .withVariable("acceptSubtypes", false)
        .run();
  }

  @Test
  public void validateExactTypeSuccess() throws Exception {
    String result = (String) flowRunner("validateType")
        .withVariable("instance", new ExecutableElement())
        .withVariable("acceptSubtypes", false)
        .run()
        .getMessage().getPayload().getValue();
    assertThat(result, is("SUCCESS"));
  }

  @Test
  public void validateSubtypesFails() throws Exception {
    expectedError.expectError("JAVA", "WRONG_INSTANCE_CLASS", WrongTypeModuleException.class,
                              format("Expected an instance of type [%s] but was [%s]",
                                     ExecutableElement.class.getName(), CompositePojo.class.getName()));

    flowRunner("validateType")
        .withVariable("instance", new CompositePojo())
        .withVariable("acceptSubtypes", true)
        .run();
  }

  @Test
  public void validateSubtypesSuccess() throws Exception {
    String result = (String) flowRunner("validateType")
        .withVariable("instance", new ChildOfExecutableElement())
        .withVariable("acceptSubtypes", true)
        .run()
        .getMessage().getPayload().getValue();
    assertThat(result, is("SUCCESS"));
  }

}
