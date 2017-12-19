/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.internal.operation;

import static java.lang.String.format;
import static java.lang.reflect.Modifier.isPublic;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import org.mule.extensions.java.api.exception.ArgumentMismatchModuleException;
import org.mule.extensions.java.api.exception.InvocationModuleException;
import org.mule.extensions.java.api.exception.NoSuchConstructorModuleException;
import org.mule.extensions.java.api.exception.NonInstantiableTypeModuleException;
import org.mule.extensions.java.internal.JavaModule;
import org.mule.extensions.java.internal.error.JavaNewInstanceErrorProvider;
import org.mule.extensions.java.internal.metadata.ConstructorTypeResolver;
import org.mule.extensions.java.internal.parameters.ConstructorIdentifier;
import org.mule.extensions.java.internal.parameters.ExecutableIdentifier;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.core.api.util.ClassUtils;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.error.Throws;
import org.mule.runtime.extension.api.annotation.metadata.MetadataKeyId;
import org.mule.runtime.extension.api.annotation.metadata.OutputResolver;
import org.mule.runtime.extension.api.annotation.metadata.TypeResolver;
import org.mule.runtime.extension.api.annotation.param.Content;
import org.mule.runtime.extension.api.annotation.param.NullSafe;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Defines the operations of {@link JavaModule} related to dynamic objects instantiation.
 *
 * @since 1.0
 */
public class JavaNewInstanceOperation {

  @Alias("new")
  @Throws(JavaNewInstanceErrorProvider.class)
  @OutputResolver(output = ConstructorTypeResolver.class)
  public Object newInstance(
                            @ParameterGroup(
                                name = "Constructor") @MetadataKeyId(ConstructorTypeResolver.class) ConstructorIdentifier identifier,
                            @Optional @NullSafe @Content @TypeResolver(ConstructorTypeResolver.class) LinkedHashMap<String, TypedValue<Object>> args)
      throws ClassNotFoundException, IllegalAccessException, NoSuchConstructorModuleException,
      ArgumentMismatchModuleException, NonInstantiableTypeModuleException, InvocationModuleException {


    final Class targetClass = ClassUtils.loadClass(identifier.getClazz(), getClass());

    List<Constructor> constructors = stream(targetClass.getConstructors())
        .filter(c -> isPublic(c.getModifiers()))
        .collect(toList());

    Constructor constructor = constructors.stream()
        .filter(identifier::matches)
        .findFirst()
        .orElseThrow(() -> new NoSuchConstructorModuleException(identifier, targetClass.getName(), constructors, args));

    try {
      List<Object> sortedArgs = JavaModuleUtils.getSortedArgs(args, constructor.getParameters());
      if (sortedArgs.size() != constructor.getParameters().length) {
        throw new ArgumentMismatchModuleException(failureMsg(identifier), constructor, args);
      }

      return constructor.newInstance(sortedArgs.toArray());

    } catch (IllegalArgumentException e) {
      throw new ArgumentMismatchModuleException(failureMsg(identifier), constructor, args, e);
    } catch (InstantiationException e) {
      throw new NonInstantiableTypeModuleException(identifier, args, e);
    } catch (InvocationTargetException e) {
      throw new InvocationModuleException(failureMsg(identifier), args, e);
    }
  }

  private String failureMsg(ExecutableIdentifier identifier) {
    return format("Failed to instantiate Class [%s]", identifier.getClazz());
  }

}
