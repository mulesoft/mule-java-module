/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.internal.metadata;

import static java.lang.reflect.Modifier.isStatic;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import org.mule.extensions.java.internal.parameters.StaticMethodIdentifier;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.metadata.MetadataContext;
import org.mule.runtime.api.metadata.MetadataKey;
import org.mule.runtime.api.metadata.MetadataResolvingException;
import org.mule.runtime.api.metadata.resolving.InputTypeResolver;
import org.mule.runtime.api.metadata.resolving.OutputTypeResolver;
import org.mule.runtime.api.metadata.resolving.PartialTypeKeysResolver;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.List;

/**
 * An {@link InputTypeResolver}, {@link OutputTypeResolver} and {@link PartialTypeKeysResolver}
 * that provides metadata related to {@code static} {@link Method}s of a given {@link Class}.
 *
 * @since 1.0
 */
public class StaticMethodTypeResolver extends ExecutableElementTypeResolver
    implements PartialTypeKeysResolver<StaticMethodIdentifier> {

  @Override
  public MetadataKey resolveChilds(MetadataContext metadataContext, StaticMethodIdentifier key)
      throws MetadataResolvingException, ConnectionException {

    return buildMethodKeys(key.getClazz());
  }

  @Override
  protected List<Executable> getExecutableElements(Class<?> targetClass) {
    return stream(targetClass.getMethods())
        .filter(m -> isStatic(m.getModifiers()))
        .collect(toList());
  }

  @Override
  public String getResolverName() {
    return "StaticMethodTypeResolver";
  }

  @Override
  public String getCategoryName() {
    return "StaticMethodTypes";
  }
}
