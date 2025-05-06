/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.internal.metadata;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mule.metadata.api.model.MetadataFormat.JAVA;
import static org.mule.runtime.api.component.location.Location.builder;
import static org.mule.runtime.api.metadata.MetadataService.METADATA_SERVICE_KEY;

import org.mule.extensions.internal.JavaModuleAbstractTestCase;
import org.mule.metadata.api.ClassTypeLoader;
import org.mule.metadata.api.builder.BaseTypeBuilder;
import org.mule.metadata.api.model.MetadataType;
import org.mule.metadata.api.model.ObjectType;
import org.mule.runtime.api.component.location.Location;
import org.mule.runtime.api.meta.model.operation.OperationModel;
import org.mule.runtime.api.meta.model.parameter.ParameterModel;
import org.mule.runtime.api.metadata.MetadataKey;
import org.mule.runtime.api.metadata.MetadataKeysContainer;
import org.mule.runtime.api.metadata.MetadataService;
import org.mule.runtime.api.metadata.descriptor.ComponentMetadataDescriptor;
import org.mule.runtime.api.metadata.resolving.MetadataFailure;
import org.mule.runtime.api.metadata.resolving.MetadataResult;
import org.mule.runtime.extension.api.declaration.type.ExtensionsTypeLoaderFactory;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Before;

public abstract class AbstractMetadataTestCase extends JavaModuleAbstractTestCase {

  static final String INSTANCE_CATEGORY = "InstanceMethodTypes";
  static final String STATIC_CATEGORY = "StaticMethodTypes";
  static final String CONSTRUCTOR_CATEGORY = "ConstructorTypes";

  static final String NEW = "new";
  static final String INVOKE = "invoke";
  static final String INVOKE_STATIC = "invokeStatic";

  static final String EXECUTABLE_ELEMENT = "ExecutableElement";
  static final String GET_NULL = "getNull";
  static final String SAY_HI = "sayHi()";
  static final String SAY_HI_STRING_INT = "sayHi(String,int)";
  static final String NEXT_PHASE = "nextPhase()";
  static final String CHILD_EXECUTABLE_ELEMENT = "ChildOfExecutableElement";

  static final String COMPLEX_RETURN_TYPES = "ComplexReturnTypes";
  static final String GET_FUNCTION = "getFunction()";
  static final String GET_TYPED_VALUE = "getTypedValue(String)";
  static final String TRIPLE_STATE_BOOLEAN = "tripleStateBoolean(boolean)";

  static final String COMPOSITE_POJO = "CompositePojo";
  static final String MAP = "Map";
  static final String SET_CHILDS = "setChilds(Map)";
  static final String CREATE = "create";
  static final String PHASE = "Phase";
  static final String OBJECT = "Object";

  @Inject
  @Named(METADATA_SERVICE_KEY)
  protected MetadataService service;

  protected ClassTypeLoader typeLoader;
  protected BaseTypeBuilder typeBuilder;

  @Override
  public boolean enableLazyInit() {
    return true;
  }

  @Override
  protected String getConfigFile() {
    return "java-metadata-config.xml";
  }

  @Override
  public boolean disableXmlValidations() {
    return true;
  }

  @Override
  protected boolean isDisposeContextPerClass() {
    return false;
  }

  @Before
  public void setLoadersContext() {
    this.typeLoader = ExtensionsTypeLoaderFactory.getDefault().createTypeLoader();
    this.typeBuilder = BaseTypeBuilder.create(JAVA);
  }

  protected String flow(String operation, String clazz) {
    return "%s%s".formatted(operation, clazz);
  }

  protected String flow(String operation, String clazz, String method) {
    method = method.replaceAll("\\(", "").replaceAll("\\)", "").replaceAll(",", "").trim();
    return "%s%s%s".formatted(operation, clazz, method);
  }

  protected String flow(String operation, String clazz, String method, String args) {
    method = method.replaceAll("\\(", "").replaceAll("\\)", "");
    return "%s%s%s%s".formatted(operation, clazz, method, args);
  }

  protected Set<MetadataKey> getKeys(String flow, String category) {
    MetadataResult<MetadataKeysContainer> result = service.getMetadataKeys(location(flow));
    assertSuccess(result);
    assertThat(result.get().getKeysByCategory().containsKey(category), is(true));
    return result.get().getKeysByCategory().get(category);
  }

  protected OperationModel getMetadata(String flow) {
    MetadataResult<ComponentMetadataDescriptor<OperationModel>> result =
        service.getOperationMetadata(location(flow));
    assertSuccess(result);
    return result.get().getModel();
  }

  private void assertSuccess(MetadataResult<?> result) {
    assertThat(result.getFailures().stream().map(MetadataFailure::getMessage).collect(Collectors.joining(", ")),
               result.isSuccess(), is(true));
  }

  protected Location location(String flow) {
    return builder().globalName(flow).addProcessorsPart().addIndexPart(0).build();
  }

  protected ObjectType toObjectType(MetadataType type) {
    assertThat(type, is(instanceOf(ObjectType.class)));
    return (ObjectType) type;
  }

  protected MetadataType getParameterType(List<ParameterModel> parameters, String name) {
    return parameters.stream().filter(p -> p.getName().equals(name)).findFirst().get().getType();
  }

}
