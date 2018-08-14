/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.internal.metadata;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.lang.reflect.Modifier.isPublic;
import static java.util.Arrays.stream;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;
import static org.mule.runtime.api.metadata.MetadataKeyBuilder.newKey;
import static org.mule.runtime.api.metadata.resolving.FailureCode.INVALID_METADATA_KEY;
import static org.mule.runtime.core.api.util.StringUtils.isBlank;
import org.mule.extensions.java.internal.parameters.ExecutableIdentifier;
import org.mule.extensions.java.internal.parameters.ExecutableIdentifierFactory;
import org.mule.metadata.api.ClassTypeLoader;
import org.mule.metadata.api.builder.ObjectTypeBuilder;
import org.mule.metadata.api.model.MetadataType;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.metadata.MetadataContext;
import org.mule.runtime.api.metadata.MetadataKey;
import org.mule.runtime.api.metadata.MetadataKeyBuilder;
import org.mule.runtime.api.metadata.MetadataResolvingException;
import org.mule.runtime.api.metadata.resolving.InputTypeResolver;
import org.mule.runtime.api.metadata.resolving.OutputTypeResolver;
import org.mule.runtime.api.metadata.resolving.TypeKeysResolver;
import org.mule.runtime.core.api.util.ClassUtils;
import org.mule.runtime.extension.api.declaration.type.ExtensionsTypeLoaderFactory;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Base {@link InputTypeResolver}, {@link OutputTypeResolver} and {@link TypeKeysResolver} for any {@link Executable} element.
 *
 * @since 1.0
 */
abstract class ExecutableElementTypeResolver implements OutputTypeResolver<ExecutableIdentifier>,
    InputTypeResolver<ExecutableIdentifier>,
    TypeKeysResolver {

  @Override
  public String getCategoryName() {
    return "MethodTypes";
  }

  @Override
  public String getResolverName() {
    return "MethodTypeResolver";
  }

  protected abstract List<Executable> getExecutableElements(Class<?> targetClass);

  @Override
  public MetadataType getInputMetadata(MetadataContext ctx, ExecutableIdentifier key)
      throws MetadataResolvingException, ConnectionException {

    Executable element = findElement(key);

    if (element.getParameters().length == 0) {
      return ctx.getTypeBuilder().nullType().build();
    }

    ObjectTypeBuilder inputParameters = ctx.getTypeBuilder().objectType().id(key.getElementId() + "_INPUT");
    stream(element.getParameters())
        .forEach(param -> inputParameters.addField()
            .key(param.getName())
            .value(getTypeLoader(ctx, param).load(param.getType())));

    return inputParameters.build();
  }

  private ClassTypeLoader getTypeLoader(MetadataContext context, Parameter param) {
    return param.getType().getClassLoader() == null
        ? context.getTypeLoader()
        : ExtensionsTypeLoaderFactory.getDefault().createTypeLoader(param.getType().getClassLoader());
  }

  @Override
  public MetadataType getOutputType(MetadataContext context, ExecutableIdentifier key)
      throws MetadataResolvingException, ConnectionException {

    Executable element = findElement(key);
    Type output = element instanceof Method
        ? ((Method) element).getGenericReturnType()
        : loadClass(key.getClazz());

    return context.getTypeLoader().load(output);
  }

  @Override
  public Set<MetadataKey> getKeys(MetadataContext context)
      throws MetadataResolvingException, ConnectionException {
    return emptySet();
  }

  private Executable findElement(ExecutableIdentifier key) throws MetadataResolvingException {

    if (isBlank(key.getClazz())) {
      throw new MetadataResolvingException("Missing Class name", INVALID_METADATA_KEY);
    }

    if (isBlank(key.getElementId())) {
      throw new MetadataResolvingException("Missing Method name", INVALID_METADATA_KEY);
    }

    Class<?> targetClass = loadClass(key.getClazz());

    return getExecutableElements(targetClass).stream()
        .filter(m -> isPublic(m.getModifiers()))
        .filter(m -> hasExpectedSignature(m, key))
        .findFirst()
        .orElseThrow(() -> new MetadataResolvingException(format("No public Method found in Class [%s] with signature [%s]",
                                                                 key.getClazz(), key.getElementId()),
                                                          INVALID_METADATA_KEY));
  }

  private boolean hasExpectedSignature(Executable m, ExecutableIdentifier key) {
    return key.matches(m);
  }

  protected MetadataKey buildMethodKeys(String clazz) throws MetadataResolvingException {
    if (isBlank(clazz)) {
      throw new MetadataResolvingException("Missing Class name. Cannot resolve Methods without a target Class",
                                           INVALID_METADATA_KEY);
    }

    Map<String, List<Executable>> executableElements = new HashMap<>();
    Class targetClass = loadClass(clazz);

    List<MetadataKey> methods = new LinkedList<>();
    getExecutableElements(targetClass).stream()
        .filter(method -> isPublic(method.getModifiers()))
        .forEach(method -> executableElements
            .computeIfAbsent(buildOverloadedMethodKey(method, emptySet()).getDisplayName(), key -> new LinkedList<>())
            .add(method));

    executableElements
        .forEach((simpleDisplayName,
                  executablesWithNameSimpleDisplayName) -> addMetadataKeysWithSameSimpleName(executablesWithNameSimpleDisplayName,
                                                                                             methods));

    MetadataKeyBuilder key = MetadataKeyBuilder.newKey(clazz).withDisplayName(targetClass.getSimpleName());
    methods.forEach(key::withChild);
    return key.build();
  }

  private Class<?> loadClass(String className) throws MetadataResolvingException {
    try {
      return ClassUtils.loadClass(className, Thread.currentThread().getContextClassLoader());
    } catch (ClassNotFoundException e) {
      throw new MetadataResolvingException(format("Failed to load Class with name [%s]: %s",
                                                  className, e.getMessage()),
                                           INVALID_METADATA_KEY);
    }
  }

  private void addMetadataKeysWithSameSimpleName(List<Executable> executablesWithNameSimpleDisplayName,
                                                 List<MetadataKey> methods) {
    if (executablesWithNameSimpleDisplayName.size() == 1) {
      methods.add(buildOverloadedMethodKey(executablesWithNameSimpleDisplayName.get(0), emptySet()));
    } else {
      Set<Integer> fqnIndexes = getParameterIndexesThatNeedFqn(executablesWithNameSimpleDisplayName);
      executablesWithNameSimpleDisplayName.forEach(method -> methods.add(buildOverloadedMethodKey(method, fqnIndexes)));
    }
  }

  private Set<Integer> getParameterIndexesThatNeedFqn(List<Executable> executablesWithNameSimpleDisplayName) {
    Set<Integer> indexes = new HashSet<>();
    Executable firstExecutable = executablesWithNameSimpleDisplayName.get(0);
    for (int parameterIndex = 0; parameterIndex < firstExecutable.getParameterTypes().length; parameterIndex++) {
      for (int methodIndex = 1; methodIndex < executablesWithNameSimpleDisplayName.size(); methodIndex++) {
        if (parameterTypeCanonicanNamesDiffer(firstExecutable, executablesWithNameSimpleDisplayName.get(methodIndex),
                                              parameterIndex)) {
          indexes.add(parameterIndex);
          break;
        }
      }
    }
    return indexes;
  }

  private boolean parameterTypeCanonicanNamesDiffer(Executable firstExecutable, Executable secondExecutable, int parameterIndex) {
    return !firstExecutable.getParameterTypes()[parameterIndex].getCanonicalName()
        .equals(secondExecutable.getParameterTypes()[parameterIndex].getCanonicalName());
  }

  private MetadataKey buildOverloadedMethodKey(Executable method, Set<Integer> parameterIndexesThatNeedFqn) {

    Parameter[] parameters = method.getParameters();
    List<String> argTypes = new LinkedList<>();
    for (int parameterIndex = 0; parameterIndex < method.getParameterTypes().length; parameterIndex++) {
      Parameter parameter = parameters[parameterIndex];
      if (parameterIndexesThatNeedFqn.contains(parameterIndex)) {
        argTypes.add(parameter.getType().getCanonicalName() + " " + parameter.getName());
      } else {
        argTypes.add(parameter.getType().getSimpleName() + " " + parameter.getName());
      }
    }

    ExecutableIdentifier identifier = ExecutableIdentifierFactory.create(method);
    String displayName = format("%s(%s)", identifier.getElementName(), join(", ", argTypes));
    return newKey(identifier.getElementId()).withDisplayName(displayName).build();
  }
}
