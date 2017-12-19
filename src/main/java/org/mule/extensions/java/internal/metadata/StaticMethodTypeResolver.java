/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.internal.metadata;

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
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
    return Arrays.stream(targetClass.getMethods())
        .filter(m -> Modifier.isStatic(m.getModifiers()))
        .collect(Collectors.toList());
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
