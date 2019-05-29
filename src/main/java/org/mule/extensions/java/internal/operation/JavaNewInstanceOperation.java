/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.internal.operation;

import static java.lang.String.format;
import static org.mule.extensions.java.internal.util.JavaModuleUtils.getSortedAndTransformedArgs;
import static org.mule.extensions.java.internal.util.JavaModuleUtils.logTooManyArgsWarning;
import static org.mule.runtime.api.meta.model.operation.ExecutionType.CPU_INTENSIVE;

import org.mule.extensions.java.api.exception.ArgumentMismatchModuleException;
import org.mule.extensions.java.api.exception.ClassNotFoundModuleException;
import org.mule.extensions.java.api.exception.InvocationModuleException;
import org.mule.extensions.java.api.exception.NoSuchConstructorModuleException;
import org.mule.extensions.java.api.exception.NonInstantiableTypeModuleException;
import org.mule.extensions.java.internal.JavaModule;
import org.mule.extensions.java.internal.cache.JavaModuleLoadingCache;
import org.mule.extensions.java.internal.error.JavaNewInstanceErrorProvider;
import org.mule.extensions.java.internal.metadata.ConstructorTypeResolver;
import org.mule.extensions.java.internal.parameters.ConstructorIdentifier;
import org.mule.extensions.java.internal.parameters.ExecutableIdentifier;
import org.mule.extensions.java.internal.transformer.ParametersTransformationResult;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.transformation.TransformationService;
import org.mule.runtime.core.api.el.ExpressionManager;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.error.Throws;
import org.mule.runtime.extension.api.annotation.execution.Execution;
import org.mule.runtime.extension.api.annotation.metadata.MetadataKeyId;
import org.mule.runtime.extension.api.annotation.metadata.OutputResolver;
import org.mule.runtime.extension.api.annotation.metadata.TypeResolver;
import org.mule.runtime.extension.api.annotation.param.Content;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.NullSafe;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines the operations of {@link JavaModule} related to dynamic objects instantiation.
 *
 * @since 1.0
 */
public class JavaNewInstanceOperation {

  private static final Logger LOGGER = LoggerFactory.getLogger(JavaNewInstanceOperation.class);

  @Inject
  private JavaModuleLoadingCache cache;

  @Inject
  private TransformationService transformationService;

  @Inject
  private ExpressionManager expressionManager;

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
  @MediaType(value = "application/java")
  @Throws(JavaNewInstanceErrorProvider.class)
  @OutputResolver(output = ConstructorTypeResolver.class)
  @Execution(CPU_INTENSIVE)
  public Object newInstance(
                            @ParameterGroup(
                                name = "Constructor") @MetadataKeyId(ConstructorTypeResolver.class) ConstructorIdentifier identifier,
                            @Optional @NullSafe @Content @TypeResolver(ConstructorTypeResolver.class) Map<String, TypedValue<Object>> args)
      throws ClassNotFoundModuleException, NoSuchConstructorModuleException, ArgumentMismatchModuleException,
      InvocationModuleException, NonInstantiableTypeModuleException {

    final Class<?> targetClass = cache.loadClass(identifier.getClazz());
    final Constructor constructor = cache.getConstructor(identifier, targetClass, args);
    ParametersTransformationResult transformationResult = getSortedAndTransformedArgs(args, constructor,
                                                                                      transformationService, expressionManager,
                                                                                      LOGGER);

    if (constructor.getParameters().length > args.size()) {
      throw new ArgumentMismatchModuleException(getBaseFailure(identifier) +
          ". Too few arguments were provided for the invocation",
                                                constructor, args, transformationResult);

    } else if (constructor.getParameters().length < args.size()) {
      logTooManyArgsWarning(constructor, args, identifier, LOGGER);
    }

    if (transformationResult.isSuccess()) {
      return invokeNew(identifier, args, constructor, transformationResult);
    }

    throw new ArgumentMismatchModuleException(getBaseFailure(identifier) +
        ". The given arguments could not be transformed to match those expected by the Constructor",
                                              constructor, args, transformationResult);

  }

  private Object invokeNew(ConstructorIdentifier identifier,
                           Map<String, TypedValue<Object>> args,
                           Constructor constructor,
                           ParametersTransformationResult transformationResult) {
    try {
      return constructor.newInstance(transformationResult.getTransformed().toArray());
    } catch (IllegalArgumentException e) {
      throw new ArgumentMismatchModuleException(getBaseFailure(identifier), constructor, args, transformationResult, e);
    } catch (InstantiationException e) {
      throw new NonInstantiableTypeModuleException(identifier, args, e);
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new InvocationModuleException(format("%s '%s' in Class '%s' ",
                                                 identifier.getExecutableTypeName(), identifier.getElementId(),
                                                 identifier.getClazz()),
                                          constructor, args, e);
    }
  }

  private String getBaseFailure(ExecutableIdentifier identifier) {
    return format("Failed to instantiate Class '%s' using the Constructor '%s'",
                  identifier.getClazz(), identifier.getElementId());
  }

}
