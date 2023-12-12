/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.internal.execution;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.mule.functional.api.exception.ExpectedError.none;
import org.mule.extensions.internal.JavaModuleAbstractTestCase;
import org.mule.extensions.internal.model.ExecutableElement;
import org.mule.extensions.java.api.exception.ArgumentMismatchModuleException;
import org.mule.extensions.java.api.exception.InvocationModuleException;
import org.mule.functional.api.exception.ExpectedError;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.tck.junit4.rule.SystemProperty;

import org.junit.Rule;
import org.junit.Test;

public class JavaArgumentTransformationTestCase extends JavaModuleAbstractTestCase {

  @Rule
  public SystemProperty className = new SystemProperty("className", ExecutableElement.class.getName());

  @Rule
  public ExpectedError expectedError = none();

  @Override
  protected String getConfigFile() {
    return "java-argument-transformation.xml";
  }

  @Test
  public void invokeStaticWithNumberUsingStringTransformation() throws Exception {
    TypedValue<String> payload = flowRunner("invokeStaticWithNumberUsingStringTransformation")
        .run()
        .getMessage()
        .getPayload();
    assertThat(payload.getValue(), is("3"));
  }

  @Test
  public void invokeGetCarFromListFailsWithIllegalArgument() throws Exception {
    expectedError.expectError("JAVA", "INVOCATION",
                              InvocationModuleException.class,
                              "Invocation of static Method 'getCarFromList(List, int)' "
                                  + "from Class 'org.mule.extensions.internal.model.AnotherExecutableElement' "
                                  + "with arguments [java.util.ArrayList<?> arg0, java.lang.String arg1] resulted in an error.\n"
                                  + "Expected arguments are [java.util.List<org.mule.extensions.internal.model.Car> cars, int index].\n"
                                  + "Cause: java.lang.ClassCastException -");
    flowRunner("invokeGetCarFromListWithParameterizedItem")
        .withVariable("item", "MyString")
        .run();
  }

  @Test
  public void invokeStaticWithStringUsingInputStreamTransformation() throws Exception {
    TypedValue<String> payload = flowRunner("invokeStaticWithStringUsingInputStreamTransformation")
        .run()
        .getMessage()
        .getPayload();
    assertThat(payload.getValue(), is("Hello this is getting transformed"));
  }

  @Test
  public void invokeStaticWithMapParameter() throws Exception {
    TypedValue<Integer> payload = flowRunner("invokeStaticWithMapParameter")
        .run()
        .getMessage()
        .getPayload();
    assertThat(payload.getValue(), is(4));
  }

  @Test
  public void invokeStaticWithMapParameterByPositionIndexInsteadOfName() throws Exception {
    TypedValue<Integer> payload = flowRunner("invokeStaticWithMapParameterByPositionIndexInsteadOfName")
        .run()
        .getMessage()
        .getPayload();
    assertThat(payload.getValue(), is(4));
  }

  @Test
  public void invokeStaticWithMapWithListValues() throws Exception {
    TypedValue<String> payload = flowRunner("invokeStaticWithMapWithListValues")
        .run()
        .getMessage()
        .getPayload();
    assertThat(payload.getValue(), is("11"));
  }

  @Test
  public void invokeStaticWithSpecificListImplementation() throws Exception {
    TypedValue<String> payload = flowRunner("invokeStaticWithSpecificListImplementation")
        .run()
        .getMessage()
        .getPayload();
    assertThat(payload.getValue(), is("4"));
  }

  @Test
  public void invokeStaticWithNestedLists() throws Exception {
    TypedValue<String> payload = flowRunner("invokeStaticWithNestedLists")
        .run()
        .getMessage()
        .getPayload();
    assertThat(payload.getValue(), is("3"));
  }

  @Test
  public void invokeStaticWithCustomPojo() throws Exception {
    TypedValue<String> payload = flowRunner("invokeStaticWithCustomPojo")
        .run()
        .getMessage()
        .getPayload();
    assertThat(payload.getValue(), is("Doors that open like this | / --"));
  }

  @Test
  public void invokeStaticWithListOfCustomPojoAndWithResultAfterwards() throws Exception {
    TypedValue<String> payload = flowRunner("invokeStaticWithListOfCustomPojoAndWithResultAfterwards")
        .run()
        .getMessage()
        .getPayload();
    assertThat(payload.getValue(), is("4"));
  }

  @Test
  public void invokeStaticWithListOfMapsAndStringsTransformed() throws Exception {
    TypedValue<Integer> payload = flowRunner("invokeStaticWithListOfMapsAndStringsTransformed")
        .run()
        .getMessage()
        .getPayload();
    assertThat(payload.getValue(), is(28));
  }

  @Test
  public void invokeStaticWithNullParameter() throws Exception {
    TypedValue<String> payload = flowRunner("invokeStaticWithNullParameter")
        .run()
        .getMessage()
        .getPayload();
    assertNull(payload.getValue());
  }

  @Test
  public void invokeStaticWithMapWithListValuesAndOneIsNull() throws Exception {
    TypedValue<String> payload = flowRunner("invokeStaticWithMapWithListValuesAndOneIsNull")
        .run()
        .getMessage()
        .getPayload();
    assertThat(payload.getValue(), is("11"));
  }

  @Test
  public void invokeStaticWithNullParameterForPrimitiveValue() throws Exception {
    expectedError.expectError("JAVA", "ARGUMENTS_MISMATCH", ArgumentMismatchModuleException.class,
                              "Failed to invoke static Method 'getSameNumber(int)' "
                                  + "from Class 'org.mule.extensions.internal.model.AnotherExecutableElement'. \n"
                                  + "Expected arguments are [int number] and invocation was attempted with arguments [java.lang.Object number].\n"
                                  + "Parameter 'number' cannot be assigned with null, but a null value was provided.");
    flowRunner("invokeStaticWithNullParameterForPrimitiveValue").run();
  }

  @Test
  public void invokeStaticWithManyNullParametersForPrimitiveValue() throws Exception {
    expectedError.expectError("JAVA", "ARGUMENTS_MISMATCH", ArgumentMismatchModuleException.class,
                              "Failed to invoke static Method 'getSameNumberOrZero(int,boolean)' "
                                  + "from Class 'org.mule.extensions.internal.model.AnotherExecutableElement'. \n"
                                  + "Expected arguments are [int number, boolean zero] and invocation was attempted with "
                                  + "arguments [java.lang.Object zero, java.lang.Object number].\n"
                                  + "Parameters [number, zero] cannot be assigned with null, but null values were provided.");
    flowRunner("invokeStaticWithManyNullParametersForPrimitiveValue").run();
  }

}
