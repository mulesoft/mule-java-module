/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.internal.operation;

import static java.lang.String.format;
import org.mule.extensions.java.api.exception.ArgumentMismatchModuleException;
import org.mule.extensions.java.api.exception.ClassNotFoundModuleException;
import org.mule.extensions.java.api.exception.InvocationModuleException;
import org.mule.extensions.java.api.exception.NoSuchConstructorModuleException;
import org.mule.extensions.java.api.exception.NonInstantiableTypeModuleException;
import org.mule.extensions.java.internal.JavaModule;
import org.mule.extensions.java.internal.JavaModuleUtils;
import org.mule.extensions.java.internal.cache.JavaModuleLoadingCache;
import org.mule.extensions.java.internal.error.JavaNewInstanceErrorProvider;
import org.mule.extensions.java.internal.metadata.ConstructorTypeResolver;
import org.mule.extensions.java.internal.parameters.ConstructorIdentifier;
import org.mule.extensions.java.internal.parameters.ExecutableIdentifier;
import org.mule.runtime.api.metadata.TypedValue;
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
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

/**
 * Defines the operations of {@link JavaModule} related to dynamic objects instantiation.
 *
 * @since 1.0
 */
public class JavaNewInstanceOperation {

  @Inject
  private JavaModuleLoadingCache cache;

  /**
   * Operation that allows the user to create a new instance of the given {@code class}
   * The identifier of the {@link Constructor} to be used includes the {@code class} and {@code constructor} names,
   * being the {@code constructor} a full description of its signature including the types of each parameter.
   * <p>
   * For example, if we want to invoke the constructor {@code Foo(String name, int age)}
   * which belongs to {@link Class} {@code org.bar.Foo}, then the identifier of the method will be {@code "Foo(String,int)"}
   *
   * @param identifier the unique identifier for the constructor to be invoked
   * @param args the arguments used to invoke the given {@link Constructor}
   * @return a new instance of the given {@code class}
   * @throws ClassNotFoundModuleException if the given {@code class} is not found in the current context
   * @throws NoSuchConstructorModuleException if the given {@code class} does not declare a constructor with the given signature
   * @throws ArgumentMismatchModuleException if the {@code method} requires a different set of arguments than the ones provided
   * @throws InvocationModuleException if an error occurs during the execution of the method
   * @throws NonInstantiableTypeModuleException if the given {@code class} is not instantiable
   */
  @Alias("new")
  @Throws(JavaNewInstanceErrorProvider.class)
  @OutputResolver(output = ConstructorTypeResolver.class)
  public Object newInstance(
                            @ParameterGroup(
                                name = "Constructor") @MetadataKeyId(ConstructorTypeResolver.class) ConstructorIdentifier identifier,
                            @Optional @NullSafe @Content @TypeResolver(ConstructorTypeResolver.class) Map<String, TypedValue<Object>> args)
      throws ClassNotFoundModuleException, NoSuchConstructorModuleException, ArgumentMismatchModuleException,
      InvocationModuleException, NonInstantiableTypeModuleException {

    final Class<?> targetClass = cache.loadClass(identifier.getClazz());
    final Constructor constructor = cache.getConstructor(identifier, targetClass, args);

    try {
      List<Object> sortedArgs = JavaModuleUtils.getSortedArgs(args, constructor.getParameters());
      if (sortedArgs.size() == constructor.getParameters().length) {
        return constructor.newInstance(sortedArgs.toArray());
      }

      throw new ArgumentMismatchModuleException(failureMsg(identifier), constructor, args);
    } catch (IllegalArgumentException e) {
      throw new ArgumentMismatchModuleException(failureMsg(identifier), constructor, args, e);
    } catch (InstantiationException e) {
      throw new NonInstantiableTypeModuleException(identifier, args, e);
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new InvocationModuleException(failureMsg(identifier), args, e);
    }
  }

  private String failureMsg(ExecutableIdentifier identifier) {
    return format("Failed to instantiate Class [%s]", identifier.getClazz());
  }

}
