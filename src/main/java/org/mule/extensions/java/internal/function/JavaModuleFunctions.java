/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.internal.function;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;
import static org.mule.extensions.java.internal.util.JavaModuleUtils.validateType;
import static org.mule.extensions.java.internal.util.MethodInvoker.invokeMethod;
import static org.mule.runtime.core.api.util.ClassUtils.isInstance;
import org.mule.extensions.java.api.exception.ArgumentMismatchModuleException;
import org.mule.extensions.java.api.exception.ClassNotFoundModuleException;
import org.mule.extensions.java.api.exception.InvocationModuleException;
import org.mule.extensions.java.api.exception.NoSuchMethodModuleException;
import org.mule.extensions.java.api.exception.WrongTypeModuleException;
import org.mule.extensions.java.internal.JavaModule;
import org.mule.extensions.java.internal.cache.JavaModuleLoadingCache;
import org.mule.extensions.java.internal.parameters.MethodIdentifier;
import org.mule.extensions.java.internal.util.JavaExceptionUtils;
import org.mule.runtime.api.el.ExpressionFunction;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.transformation.TransformationService;
import org.mule.runtime.core.api.el.ExpressionManager;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

import java.lang.reflect.Method;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ExpressionFunction}s exposed by the {@link JavaModule} that extends the EL
 * with further Java related functions.
 *
 * @since 1.0
 */
public class JavaModuleFunctions {

  private static final Logger LOGGER = LoggerFactory.getLogger(JavaModuleFunctions.class);

  @Inject
  private JavaModuleLoadingCache cache;

  @Inject
  private TransformationService transformationService;

  @Inject
  private ExpressionManager expressionManager;

  /**
   * Function that allows the user to invoke methods with the provided {@code args} on the given {@code instance}.
   * The identifier of the {@link Method} to be invoked includes the {@code class} and {@code method} names,
   * being the {@code method} a full description of its signature including the types of each parameter.
   * <p>
   * For example, if we want to invoke the method {@code echo} with signature {@code public String echo(String msg)}
   * which belongs to {@link Class} {@code org.bar.Foo}, then the identifier of the method will be {@code "echo(String)"}
   *
   * @param clazz      the fully qualified name of the class whose instance is being injected
   * @param methodName the unique identifier for the method to be invoked
   * @param instance   the instance on which the {@code method} will be invoked
   * @param args       the arguments used to invoke the given {@link Method}
   * @return the result of the {@link Method} invocation with the given {@code args}
   * @throws ClassNotFoundModuleException    if the given {@code class} is not found in the current context
   * @throws NoSuchMethodModuleException     if the given {@code class} does not declare a method with the given signature
   * @throws ArgumentMismatchModuleException if the {@code method} requires a different set of arguments than the ones provided
   * @throws WrongTypeModuleException        if the given {@code instance} is not an instance of the expected {@code class}
   * @throws InvocationModuleException       if an error occurs during the execution of the method
   */
  public Object invoke(
                       @Alias("class") @Summary("Fully qualified name of the Class containing the referenced Method") String clazz,
                       @Alias("method") @Summary("Represents the Method signature containing the method name and it's argument types.") String methodName,
                       Object instance,
                       @Optional Map<String, TypedValue<Object>> args)
      throws NoSuchMethodModuleException, ClassNotFoundModuleException, WrongTypeModuleException,
      ArgumentMismatchModuleException, InvocationModuleException {

    final Map<String, TypedValue<Object>> resolvedArgs = args == null
        ? emptyMap()
        : args.entrySet().stream().collect(toMap(e -> e.getKey(), e -> TypedValue.of(e.getValue())));

    validateType(clazz, instance, true, cache);

    MethodIdentifier identifier = new MethodIdentifier(clazz, methodName);
    Method method = cache.getMethod(identifier, instance.getClass(), resolvedArgs, false);
    return invokeMethod(method, resolvedArgs, instance, identifier,
                        transformationService, expressionManager, LOGGER);
  }

  /**
   * Function that allows the user to check that a given {@code instance} is an {@code instanceof} the specified {@code class}.
   *
   * @param clazz    the fully qualified name of the expected {@link Class} for the instance
   * @param instance the object whose type is expected to be an {@code instanceof} of the given {@code class}
   * @throws ClassNotFoundModuleException if the given {@code class} is not found in the current context
   */
  public boolean isInstanceOf(Object instance,
                              @Alias("class") @Summary("Fully qualified name of the Class you want to check against") String clazz)
      throws ClassNotFoundModuleException {
    return isInstance(cache.loadClass(clazz), instance);
  }

  /**
   * Function that provides a way to obtain the root cause of a given {@link Throwable}.
   *
   * @param exception the {@link Throwable} whose root cause is wanted
   * @return the root {@link Throwable cause} of the given {@code exception},
   * or {@code null} if no cause is found.
   */
  public Throwable getRootCause(Throwable exception) {
    return JavaExceptionUtils.getRootCause(exception);
  }

  /**
   * This Function returns {@code true} if the given {@link Throwable} that matches
   * the specified class in the exception cause chain.
   * If {@code acceptSubtypes} is {@code true}, subclasses of the specified class will also match.
   *
   * @param exception       the {@link Throwable} to inspect
   * @param throwableType   fully qualified name of the Class you want to check against
   * @param includeSubtypes if true, subclasses of the specified class will also result in a match
   * @return the index into the throwable chain, false if no match or null input
   */
  public boolean isCausedBy(Throwable exception,
                            @Summary("Fully qualified name of the Class you want to check against") String throwableType,
                            @Optional boolean includeSubtypes) {
    return JavaExceptionUtils.isCausedBy(exception, cache.loadClass(throwableType), includeSubtypes);
  }

}
