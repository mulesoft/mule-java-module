/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.internal.metadata;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import org.mule.extensions.internal.model.CompositePojo;
import org.mule.extensions.internal.model.ExecutableElement;
import org.mule.metadata.api.model.AnyType;
import org.mule.metadata.api.model.StringType;
import org.mule.metadata.api.model.VoidType;
import org.mule.metadata.java.api.annotation.ClassInformationAnnotation;
import org.mule.runtime.api.meta.model.operation.OperationModel;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.junit.Test;

public class OutputMetadataTestCase extends AbstractMetadataTestCase {

  @Test
  public void staticMethodObjectOutput() throws Exception {
    OperationModel metadata = getMetadata(flow(INVOKE_STATIC, EXECUTABLE_ELEMENT, GET_NULL));
    assertThat(metadata.getOutput().getType(), instanceOf(AnyType.class));
  }

  @Test
  public void instanceMethodSimpleOutput() throws Exception {
    OperationModel metadata = getMetadata(flow(INVOKE, EXECUTABLE_ELEMENT, SAY_HI_STRING_INT));
    assertThat(metadata.getOutput().getType(), instanceOf(StringType.class));
  }

  @Test
  public void instanceMethodVoid() throws Exception {
    OperationModel metadata = getMetadata(flow(INVOKE, EXECUTABLE_ELEMENT, NEXT_PHASE));
    assertThat(metadata.getOutput().getType(), instanceOf(VoidType.class));
  }

  @Test
  public void staticMethodBuilderOutput() throws Exception {
    OperationModel metadata = getMetadata(flow(INVOKE_STATIC, EXECUTABLE_ELEMENT, CREATE, PHASE));
    assertThat(metadata.getOutput().getType(), is(typeLoader.load(ExecutableElement.class)));
  }

  @Test
  public void staticMethodBuilderOutputNoArgs() throws Exception {
    OperationModel metadata = getMetadata(flow(INVOKE_STATIC, EXECUTABLE_ELEMENT, CREATE));
    assertThat(metadata.getOutput().getType(), is(typeLoader.load(ExecutableElement.class)));
  }

  @Test
  public void constructorInputParametersNoArgs() throws Exception {
    OperationModel metadata = getMetadata(flow(NEW, COMPOSITE_POJO, COMPOSITE_POJO));
    assertThat(metadata.getOutput().getType(), is(typeLoader.load(CompositePojo.class)));
  }

  @Test
  public void constructorInputParameters() throws Exception {
    OperationModel metadata = getMetadata(flow(NEW, COMPOSITE_POJO, COMPOSITE_POJO, COMPOSITE_POJO));
    assertThat(metadata.getOutput().getType(), is(typeLoader.load(CompositePojo.class)));
  }

  @Test
  public void instanceMethodOptionalReturnType() throws Exception {
    OperationModel metadata = getMetadata(flow(INVOKE, COMPLEX_RETURN_TYPES, TRIPLE_STATE_BOOLEAN));
    ClassInformationAnnotation classInfo = metadata.getOutput().getType().getAnnotation(ClassInformationAnnotation.class).get();
    assertThat(classInfo.getClassname(), is(Optional.class.getName()));
    assertThat(classInfo.getGenericTypes().get(0), is(Boolean.class.getName()));
  }

  @Test
  public void instanceMethodFunctionReturnType() throws Exception {
    OperationModel metadata = getMetadata(flow(INVOKE, COMPLEX_RETURN_TYPES, GET_FUNCTION));
    ClassInformationAnnotation classInfo = metadata.getOutput().getType().getAnnotation(ClassInformationAnnotation.class).get();
    assertThat(classInfo.getClassname(), is(Function.class.getName()));
    assertThat(classInfo.getGenericTypes().get(0), is(Integer.class.getName()));
    assertThat(classInfo.getGenericTypes().get(1), is(List.class.getName()));
  }

  @Test
  public void instanceMethodTypedValueReturnType() throws Exception {
    OperationModel metadata = getMetadata(flow(INVOKE, COMPLEX_RETURN_TYPES, GET_TYPED_VALUE));
    assertThat(metadata.getOutput().getType(), instanceOf(StringType.class));
  }

}
