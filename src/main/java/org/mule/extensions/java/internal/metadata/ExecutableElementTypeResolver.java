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
import static java.util.Collections.singletonList;
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
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.api.util.Pair;
import org.mule.runtime.core.api.util.ClassUtils;
import org.mule.runtime.extension.api.declaration.type.ExtensionsTypeLoaderFactory;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

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

    MultiMap<String, Pair<Executable, MetadataKey>> executableElements = new MultiMap<>();
    Class targetClass = loadClass(clazz);

    List<MetadataKey> methods = new LinkedList<>();
    getExecutableElements(targetClass).stream()
        .filter(method -> isPublic(method.getModifiers()))
        .forEach(method -> {
          MetadataKey metadataKeyWithSimpleNames = buildMethodKeyWithSimpleNames(method);
          executableElements
              .put(metadataKeyWithSimpleNames.getDisplayName(), new Pair(method, metadataKeyWithSimpleNames));
        });

    executableElements.keySet().stream()
        .forEach(key -> methods.addAll(getMetadataKeysWithSameSimpleName(executableElements.getAll(key))));

    MetadataKeyBuilder key = MetadataKeyBuilder.newKey(clazz).withDisplayName(targetClass.getSimpleName());
    methods.forEach(key::withChild);
    return key.build();
  }

  private List<MetadataKey> getMetadataKeysWithSameSimpleName(List<Pair<Executable, MetadataKey>> executablesWithNameSimpleDisplayName) {
    if (executablesWithNameSimpleDisplayName.size() == 1) {
      if (parameterTypesWithinExecutableClash(executablesWithNameSimpleDisplayName.get(0).getFirst())) {
        return singletonList(buildMethodKeyWithFullyQualifiedNames(executablesWithNameSimpleDisplayName.get(0).getFirst()));
      } else {
        return singletonList(executablesWithNameSimpleDisplayName.get(0).getSecond());
      }
    } else {
      return executablesWithNameSimpleDisplayName.stream()
          .map(pair -> pair.getFirst())
          .map(executable -> buildMethodKeyWithFullyQualifiedNames(executable))
          .collect(toList());
    }
  }

  private boolean parameterTypesWithinExecutableClash(Executable executable) {
    Map<String, String> paramTypes = new HashMap<>();
    for (Class type : executable.getParameterTypes()) {
      String canonicalName = type.getCanonicalName();
      String boundName = paramTypes.computeIfAbsent(type.getSimpleName(), k -> canonicalName);
      if (!canonicalName.equals(boundName)) {
        return true;
      }
    }
    return false;
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

  private MetadataKey buildMethodKeyWithSimpleNames(Executable method) {
    return buildMethodKeyWithSupliedNames(method, parameter -> parameter.getType().getSimpleName());
  }

  private MetadataKey buildMethodKeyWithFullyQualifiedNames(Executable method) {
    return buildMethodKeyWithSupliedNames(method, parameter -> parameter.getType().getCanonicalName());
  }

  private MetadataKey buildMethodKeyWithSupliedNames(Executable method, Function<Parameter, String> getTypeNameFromExecutable) {
    Parameter[] parameters = method.getParameters();
    List<String> argTypes = new LinkedList<>();
    for (int parameterIndex = 0; parameterIndex < method.getParameterTypes().length; parameterIndex++) {
      Parameter parameter = parameters[parameterIndex];
      argTypes.add(getTypeNameFromExecutable.apply(parameter) + " " + parameter.getName());
    }

    ExecutableIdentifier identifier = ExecutableIdentifierFactory.create(method);
    String displayName = format("%s(%s)", identifier.getElementName(), join(", ", argTypes));
    return newKey(identifier.getElementId()).withDisplayName(displayName).build();
  }

}
