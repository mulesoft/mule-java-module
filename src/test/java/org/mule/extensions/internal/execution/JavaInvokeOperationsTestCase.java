/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.internal.execution;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mule.functional.api.exception.ExpectedError.none;
import static org.mule.runtime.api.metadata.DataType.XML_STRING;
import static org.mule.tck.junit4.matcher.DataTypeMatcher.like;
import org.mule.extensions.internal.JavaModuleAbstractTestCase;
import org.mule.extensions.internal.model.ComplexReturnTypes;
import org.mule.extensions.internal.model.ExecutableElement;
import org.mule.extensions.java.api.exception.ArgumentMismatchModuleException;
import org.mule.extensions.java.api.exception.NoSuchMethodModuleException;
import org.mule.functional.api.exception.ExpectedError;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.tck.junit4.rule.SystemProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.junit.Rule;
import org.junit.Test;

public class JavaInvokeOperationsTestCase extends JavaModuleAbstractTestCase {

  @Rule
  public SystemProperty className = new SystemProperty("className", ExecutableElement.class.getName());

  @Rule
  public ExpectedError expectedError = none();

  @Override
  protected String getConfigFile() {
    return "java-invoke-operation.xml";
  }

  @Test
  public void invokeInstanceNoArgs() throws Exception {
    TypedValue<String> payload = invoke("sayHi()", new ExecutableElement());
    assertThat(payload.getValue(), is("Hi"));
  }

  @Test
  public void invokeInstancePojoReturn() throws Exception {
    TypedValue<ComplexReturnTypes> payload = invoke("createEmptyPojo()", new ExecutableElement());
    assertThat(payload.getValue(), is(not(nullValue())));
    assertThat(payload.getValue().getTypedValue("<node/>").getDataType(), is(like(XML_STRING)));
  }

  @Test
  public void invokeTypoMethod() throws Exception {
    expectedError.expectError("JAVA", "NO_SUCH_METHOD",
                              NoSuchMethodModuleException.class,
                              "No public Method found with signature 'createEmptyPojo' "
                                  + "for Class 'org.mule.extensions.internal.model.ExecutableElement'.\n"
                                  + "Public static Methods are [");
    invoke("createEmptyPojo", new ExecutableElement());
  }

  @Test
  public void invokeAffectsState() throws Exception {
    ExecutableElement element = new ExecutableElement();
    assertThat(element.getPhase(), is(ExecutableElement.Phase.NOT_STARTED));

    invoke("nextPhase()", element);
    TypedValue<Integer> phaseId = invoke("getPhaseId()", element);

    assertThat(phaseId.getValue(), is(ExecutableElement.Phase.STARTED.ordinal()));

    invoke("nextPhase()", element);
    TypedValue<ExecutableElement.Phase> phase = invoke("getPhase()", element);

    assertThat(phase.getValue(), is(ExecutableElement.Phase.STOPPED));
  }

  @Test
  public void invokeAffectsParameter() throws Exception {
    final Map<String, String> input = new HashMap<>();

    invoke("addToMap(Map)", new ExecutableElement(), Args.create("input", input));

    assertThat(input.get(ExecutableElement.ENRICH_KEY), is(ExecutableElement.ENRICH_VALUE));
  }

  @Test
  public void invokeResolveOverloadByType() throws Exception {
    Args args = Args.create("name", RICK);

    TypedValue<String> payload = invoke("sayHi(String)", new ExecutableElement(), args);
    assertThat(payload.getValue(), is("Hi " + RICK));

    args.add("id", RICK_ID);
    payload = invoke("sayHi(String, int)", new ExecutableElement(), args);
    assertThat(payload.getValue(), is("Hi " + RICK + "::" + RICK_ID));

    payload = invoke("sayHi(int)", new ExecutableElement(), Args.create("id", RICK_ID));
    assertThat(payload.getValue(), is("Hi " + RICK_ID));
  }

  @Test
  public void invokeInstanceValidateTypeIsCorrect() throws Exception {
    invoke("sayHi()", new ExecutableElement());
  }

  @Test
  public void invokeInstanceInlineArgs() throws Exception {
    Map<String, Object> result = (Map<String, Object>) flowRunner("invokeEnrichInputWithInlineArgs")
        .withVariable("instance", new ExecutableElement())
        .run().getMessage().getPayload().getValue();

    MatcherAssert.assertThat(result.get(ExecutableElement.ENRICH_KEY), Is.is(ExecutableElement.ENRICH_VALUE));
    assertThat(((List) result.get("aList")).get(0), is(RICK_ID));
  }

  @Test
  public void invokeOverloadedMethodWithInlineArgs() throws Exception {
    String result = (String) flowRunner("invokeOverloadedMethodWithInlineArgs")
        .withVariable("instance", new ExecutableElement())
        .run().getMessage().getPayload().getValue();

    assertThat(result, is("Hi " + RICK + "::" + RICK_ID));
  }

  @Test
  public void invokeOverloadedMethodWithInlineArgsByPositionIndexInsteadOfName() throws Exception {
    String result = (String) flowRunner("invokeOverloadedMethodWithInlineArgsByPositionIndexInsteadOfName")
        .withVariable("instance", new ExecutableElement())
        .run().getMessage().getPayload().getValue();

    assertThat(result, is("Hi " + RICK + "::" + RICK_ID));
  }

  @Test
  public void invokeResolveOverloadById() throws Exception {
    List<String> list = new LinkedList<>();
    List<String> result = (List<String>) invoke("addToList(List)", new ExecutableElement(),
                                                Args.create("list", list)).getValue();

    assertThat(result.size(), is(1));
    assertThat(result.get(0), is("List"));

    result = (List<String>) invoke("addToList(LinkedList)", new ExecutableElement(),
                                   Args.create("linkedList", list)).getValue();

    assertThat(result.size(), is(2));
    assertThat(result.get(1), is("LinkedList"));

    result = (List<String>) invoke("addToList(ArrayList)", new ExecutableElement(),
                                   Args.create("list", new ArrayList<>())).getValue();

    assertThat(result.size(), is(1));
    assertThat(result.get(0), is("ArrayList"));
  }

  @Test
  public void invokeInstanceWithMissingOneArgument() throws Exception {
    String errorMessage = "Failed to invoke Method 'sayHi(String,int)' "
        + "from Class 'org.mule.extensions.internal.model.ExecutableElement'. Too few arguments were provided for the invocation. \n"
        + "Expected arguments are [java.lang.String name, int id] and invocation was attempted with arguments [java.lang.String arg0].\n"
        + "Missing parameter [id].";

    expectedError.expectError("JAVA", "ARGUMENTS_MISMATCH", ArgumentMismatchModuleException.class, errorMessage);
    invoke("sayHi(String,int)", new ExecutableElement(), Args.create("arg0", "zaraza"));
  }

  @Test
  public void invokeInstanceWithWrongArgumentType() throws Exception {
    String errorMessage =
        "Failed to invoke Method 'sayHi(int)' from Class 'org.mule.extensions.internal.model.ExecutableElement'. "
            + "The given arguments could not be transformed to match those expected by the Method. \n"
            + "Expected arguments are [int id] and invocation was attempted with arguments [java.lang.Boolean arg0].\n"
            + "No suitable transformation was found to match the expected type for the parameter [id].";

    expectedError.expectError("JAVA", "ARGUMENTS_MISMATCH", ArgumentMismatchModuleException.class, errorMessage);
    invoke("sayHi(int)", new ExecutableElement(), Args.create("arg0", true));
  }

  @Test
  public void invokeInstanceWithWrongArgumentNamePosition() throws Exception {
    String errorMessage =
        "Failed to invoke Method 'sayHi(int)' from Class 'org.mule.extensions.internal.model.ExecutableElement'. "
            + "The given arguments could not be transformed to match those expected by the Method. \n"
            + "Expected arguments are [int id] and invocation was attempted with arguments [java.lang.Boolean arg0].\n"
            + "No suitable transformation was found to match the expected type for the parameter [id].";

    //expectedError.expectError("JAVA", "ARGUMENTS_MISMATCH", ArgumentMismatchModuleException.class, errorMessage);
    invoke("sayHi(int)", new ExecutableElement(), Args.create("arg0", 5));
  }

  @Test
  public void invokeInstanceWithWrongArgumentNameName() throws Exception {
    String errorMessage =
        "Failed to invoke Method 'sayHi(int)' from Class 'org.mule.extensions.internal.model.ExecutableElement'. "
            + "The given arguments could not be transformed to match those expected by the Method. \n"
            + "Expected arguments are [int id] and invocation was attempted with arguments [java.lang.Boolean arg0].\n"
            + "No suitable transformation was found to match the expected type for the parameter [id].";

    //expectedError.expectError("JAVA", "ARGUMENTS_MISMATCH", ArgumentMismatchModuleException.class, errorMessage);
    invoke("sayHi(int)", new ExecutableElement(), Args.create("id", 5));
  }

  @Test
  public void invokeInstanceWithMissingArgument() throws Exception {
    String errorMessage =
        "Failed to invoke Method 'sayHi(int)' from Class 'org.mule.extensions.internal.model.ExecutableElement'. "
            + "Too few arguments were provided for the invocation. \n"
            + "Expected arguments are [int id] and invocation was attempted without any argument.";

    expectedError.expectError("JAVA", "ARGUMENTS_MISMATCH", ArgumentMismatchModuleException.class, errorMessage);
    invoke("sayHi(int)", new ExecutableElement());
  }

  private <T> TypedValue<T> invoke(String method, Object instance) throws Exception {
    return flowRunner("invokeInstanceNoArgs")
        .withVariable("instance", instance)
        .withVariable("method", method)
        .run()
        .getMessage()
        .getPayload();
  }

  private <T> TypedValue<T> invoke(String method, Object instance, Args args)
      throws Exception {
    return flowRunner("invokeInstanceWithArgs")
        .withVariable("instance", instance)
        .withVariable("method", method)
        .withVariable("args", args.get())
        .run()
        .getMessage()
        .getPayload();
  }

}
