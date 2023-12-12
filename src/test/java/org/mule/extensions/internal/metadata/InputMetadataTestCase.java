/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.internal.metadata;

import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;

import org.mule.extensions.internal.model.CompositePojo;
import org.mule.extensions.internal.model.ExecutableElement;
import org.mule.metadata.api.model.MetadataType;
import org.mule.metadata.api.model.NullType;
import org.mule.metadata.api.model.NumberType;
import org.mule.metadata.api.model.ObjectFieldType;
import org.mule.metadata.api.model.ObjectType;
import org.mule.metadata.api.model.StringType;
import org.mule.runtime.api.meta.model.operation.OperationModel;

import java.util.Optional;

import org.junit.Test;

public class InputMetadataTestCase extends AbstractMetadataTestCase {

  @Test
  public void methodInputParameters() throws Exception {
    OperationModel metadata = getMetadata(flow(INVOKE, EXECUTABLE_ELEMENT, SAY_HI_STRING_INT));
    ObjectType args = getArgs(metadata);

    assertThat(getFieldType(args, "name", EXECUTABLE_ELEMENT, SAY_HI_STRING_INT), instanceOf(StringType.class));
    assertThat(getFieldType(args, "id", EXECUTABLE_ELEMENT, SAY_HI_STRING_INT), instanceOf(NumberType.class));
  }

  @Test
  public void methodInputParametersNoArgs() throws Exception {
    OperationModel metadata = getMetadata(flow(INVOKE, EXECUTABLE_ELEMENT, SAY_HI));
    MetadataType args = getParameterType(metadata.getAllParameterModels(), "args");
    assertThat(args, instanceOf(NullType.class));
  }

  @Test
  public void staticMethodInputParameters() throws Exception {
    OperationModel metadata = getMetadata(flow(INVOKE_STATIC, EXECUTABLE_ELEMENT, CREATE, PHASE));
    ObjectType args = getArgs(metadata);
    assertThat(getFieldType(args, "initPhase", EXECUTABLE_ELEMENT, CREATE + PHASE),
               is(typeLoader.load(ExecutableElement.Phase.class)));
  }

  @Test
  public void staticMethodInputObjectParameter() throws Exception {
    OperationModel metadata = getMetadata(flow(INVOKE_STATIC, EXECUTABLE_ELEMENT, CREATE, OBJECT));
    ObjectType args = getArgs(metadata);
    assertThat(getFieldType(args, "phase", EXECUTABLE_ELEMENT, CREATE),
               is(typeBuilder.anyType().build()));
  }

  @Test
  public void staticMethodInputParametersNoArgs() throws Exception {
    OperationModel metadata = getMetadata(flow(INVOKE_STATIC, EXECUTABLE_ELEMENT, CREATE));
    MetadataType args = getParameterType(metadata.getAllParameterModels(), "args");
    assertThat(args, instanceOf(NullType.class));
  }

  @Test
  public void constructorInputParametersNoArgs() throws Exception {
    OperationModel metadata = getMetadata(flow(NEW, COMPOSITE_POJO, COMPOSITE_POJO));
    MetadataType args = getParameterType(metadata.getAllParameterModels(), "args");
    assertThat(args, instanceOf(NullType.class));
  }

  @Test
  public void constructorInputParameters() throws Exception {
    OperationModel metadata = getMetadata(flow(NEW, COMPOSITE_POJO, COMPOSITE_POJO, COMPOSITE_POJO));
    ObjectType args = getArgs(metadata);
    assertThat(getFieldType(args, "child", COMPOSITE_POJO, COMPOSITE_POJO), is(typeLoader.load(CompositePojo.class)));
  }

  @Test
  public void constructorInputParametersOverload() throws Exception {
    OperationModel metadata = getMetadata(flow(NEW, COMPOSITE_POJO, COMPOSITE_POJO, "String"));
    ObjectType args = getArgs(metadata);
    assertThat(getFieldType(args, "name", COMPOSITE_POJO, COMPOSITE_POJO + "String"), instanceOf(StringType.class));
  }

  private MetadataType getFieldType(ObjectType object, String name, String clazz, String method) {
    Optional<ObjectFieldType> field = object.getFieldByName(name);
    if (!field.isPresent()) {
      fail(format("Missing name parameter for %s %s", clazz, method));
    }

    return field.get().getValue();
  }

  private ObjectType getArgs(OperationModel metadata) {
    MetadataType args = getParameterType(metadata.getAllParameterModels(), "args");
    assertThat(args, instanceOf(ObjectType.class));
    return (ObjectType) args;
  }

}
