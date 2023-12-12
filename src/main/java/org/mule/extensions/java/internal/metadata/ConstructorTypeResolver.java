/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.internal.metadata;

import org.mule.extensions.java.internal.parameters.ConstructorIdentifier;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.metadata.MetadataContext;
import org.mule.runtime.api.metadata.MetadataKey;
import org.mule.runtime.api.metadata.MetadataResolvingException;
import org.mule.runtime.api.metadata.resolving.InputTypeResolver;
import org.mule.runtime.api.metadata.resolving.OutputTypeResolver;
import org.mule.runtime.api.metadata.resolving.PartialTypeKeysResolver;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.util.Arrays;
import java.util.List;

/**
 * An {@link InputTypeResolver}, {@link OutputTypeResolver} and {@link PartialTypeKeysResolver}
 * that provides metadata related to {@link Constructor}s of a given {@link Class}.
 *
 * @since 1.0
 */
public class ConstructorTypeResolver extends ExecutableElementTypeResolver
    implements PartialTypeKeysResolver<ConstructorIdentifier> {

  @Override
  public MetadataKey resolveChilds(MetadataContext metadataContext, ConstructorIdentifier key)
      throws MetadataResolvingException, ConnectionException {

    return buildMethodKeys(key.getClazz());
  }

  @Override
  protected List<Executable> getExecutableElements(Class<?> targetClass) {
    return Arrays.asList(targetClass.getConstructors());
  }

  @Override
  public String getResolverName() {
    return "ConstructorTypeResolver";
  }

  @Override
  public String getCategoryName() {
    return "ConstructorTypes";
  }
}
