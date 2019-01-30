/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.internal.operation;

import static org.mule.extensions.java.internal.util.JavaModuleUtils.validateType;
import static org.mule.extensions.java.internal.util.MethodInvoker.invokeMethod;
import static org.mule.runtime.api.meta.model.operation.ExecutionType.CPU_INTENSIVE;

import org.mule.extensions.java.api.exception.ArgumentMismatchModuleException;
import org.mule.extensions.java.api.exception.ClassNotFoundModuleException;
import org.mule.extensions.java.api.exception.InvocationModuleException;
import org.mule.extensions.java.api.exception.NoSuchMethodModuleException;
import org.mule.extensions.java.api.exception.WrongTypeModuleException;
import org.mule.extensions.java.internal.JavaModule;
import org.mule.extensions.java.internal.cache.JavaModuleLoadingCache;
import org.mule.extensions.java.internal.error.JavaInvokeErrorProvider;
import org.mule.extensions.java.internal.error.JavaStaticInvokeErrorProvider;
import org.mule.extensions.java.internal.metadata.InstanceMethodTypeResolver;
import org.mule.extensions.java.internal.metadata.StaticMethodTypeResolver;
import org.mule.extensions.java.internal.parameters.MethodIdentifier;
import org.mule.extensions.java.internal.parameters.StaticMethodIdentifier;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.transformation.TransformationService;
import org.mule.runtime.core.api.el.ExpressionManager;
import org.mule.runtime.extension.api.annotation.error.Throws;
import org.mule.runtime.extension.api.annotation.execution.Execution;
import org.mule.runtime.extension.api.annotation.metadata.MetadataKeyId;
import org.mule.runtime.extension.api.annotation.metadata.OutputResolver;
import org.mule.runtime.extension.api.annotation.metadata.TypeResolver;
import org.mule.runtime.extension.api.annotation.param.Content;
import org.mule.runtime.extension.api.annotation.param.DefaultEncoding;
import org.mule.runtime.extension.api.annotation.param.NullSafe;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.runtime.extension.api.runtime.operation.Result;

import java.lang.reflect.Method;
import java.util.Map;

import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines the operations of {@link JavaModule} related to invocation of class or instance methods using reflection.
 *
 * @since 1.0
 */
public class JavaInvokeOperations {

  private static final Logger LOGGER = LoggerFactory.getLogger(JavaInvokeOperations.class);
  private static final String MIME_TYPE_TAB = "MIME Type";

  @Inject
  private JavaModuleLoadingCache cache;

  @Inject
  private TransformationService transformationService;

  @Inject
  private ExpressionManager expressionManager;

  /**
   * Operation that allows the user to invoke static methods with the provided arguments.
   * The identifier of the {@link Method} to be invoked includes the {@code class} and {@code method} names,
   * being the {@code method} a full description of its signature including the types of each parameter.
   * <p>
   * For example, if we want to invoke the static method {@code echo} with signature {@code public static String echo(String msg)}
   * which belongs to {@link Class} {@code org.bar.Foo}, then the identifier of the method will be {@code "echo(String)"}
   *
   * @param identifier the unique identifier for the method to be invoked.
   * @param args       the arguments used to invoke the given {@link Method}. This parameter must be a map with the names
   *                   of the method arguments as keys, and each of the argument values as values.
   *                   For example, given a method {@code echo} with signature {@code public String echo(String msg, String volume)} a
   *                   possible value for args is:
   *                   #[{
   *                          msg : 'This is a message to echo',
   *                          volume : 'Loud'
   *                    }]
   *                   In order to use those argument names the java class must be compiled with -parameters, in other case
   *                   you have to use the canonical names for the arguments:
   *                   #[{
   *                        arg0 : 'This is a message to echo',
   *                        arg1 : 'Loud'
   *                   }]
   *                   You can always use the canonical name for the arguments, even if the source code was compiled with
   *                   -parameters.
   * @param outputMimeType The mime type of the payload that this invocation will output
   * @param outputEncoding The encoding of the payload that this invocation will output
   * @return the result of the {@link Method} invocation with the given {@code args}
   * @throws ClassNotFoundModuleException    if the given {@code class} is not found in the current context
   * @throws NoSuchMethodModuleException     if the given {@code class} does not declare a method with the given signature
   * @throws ArgumentMismatchModuleException if the {@code method} requires a different set of arguments than the ones provided
   * @throws InvocationModuleException       if an error occurs during the execution of the method
   */
  @Throws(JavaStaticInvokeErrorProvider.class)
  @OutputResolver(output = StaticMethodTypeResolver.class)
  @Execution(CPU_INTENSIVE)
  public Result<Object, Void> invokeStatic(
                                           @ParameterGroup(
                                               name = "Method") @MetadataKeyId(StaticMethodTypeResolver.class) StaticMethodIdentifier identifier,
                                           @Optional @NullSafe @Content @TypeResolver(StaticMethodTypeResolver.class) Map<String, TypedValue<Object>> args,
                                           @Optional @Placement(
                                               tab = MIME_TYPE_TAB) @Summary("The mime type of the payload that this invocation will output") String outputMimeType,
                                           @Optional @Placement(
                                               tab = MIME_TYPE_TAB) @Summary("The encoding of the payload that this invocation will output") String outputEncoding,
                                           @DefaultEncoding String defaultEncoding)
      throws ClassNotFoundModuleException, ArgumentMismatchModuleException,
      InvocationModuleException, NoSuchMethodModuleException {

    Class<?> targetClass = cache.loadClass(identifier.getClazz());
    Method method = cache.getMethod(identifier, targetClass, args, true);
    return invokeMethod(method, args, null, identifier, outputMimeType, outputEncoding,
                        transformationService, expressionManager, LOGGER, defaultEncoding);
  }

  /**
   * Operation that allows the user to invoke methods with the provided {@code args} on the given {@code instance}.
   * The identifier of the {@link Method} to be invoked includes the {@code class} and {@code method} names,
   * being the {@code method} a full description of its signature including the types of each parameter.
   * <p>
   * For example, if we want to invoke the method {@code echo} with signature {@code public String echo(String msg)}
   * which belongs to {@link Class} {@code org.bar.Foo}, then the identifier of the method will be {@code "echo(String)"}
   *
   * @param identifier the unique identifier for the method to be invoked
   * @param instance   the instance on which the {@code method} will be invoked
   * @param args       the arguments used to invoke the given {@link Method}. This parameter must be a map with the names
   *                   of the method arguments as keys, and each of the argument values as values.
   *                   For example, given a method {@code echo} with signature {@code public String echo(String msg, String volume)} a
   *                   possible value for args is:
   *                   #[{
   *                          msg : 'This is a message to echo',
   *                          volume : 'Loud'
   *                    }]
   *                   In order to use those argument names the java class must be compiled with -parameters, in other case
   *                   you have to use the canonical names for the arguments:
   *                   #[{
   *                        arg0 : 'This is a message to echo',
   *                        arg1 : 'Loud'
   *                   }]
   *                   You can always use the canonical name for the arguments, even if the source code was compiled with
   *                   -parameters.
   * @param outputMimeType The mime type of the payload that this invocation will output
   * @param outputEncoding The encoding of the payload that this invocation will output
   * @return the result of the {@link Method} invocation with the given {@code args}
   * @throws ClassNotFoundModuleException    if the given {@code class} is not found in the current context
   * @throws NoSuchMethodModuleException     if the given {@code class} does not declare a method with the given signature
   * @throws ArgumentMismatchModuleException if the {@code method} requires a different set of arguments than the ones provided
   * @throws WrongTypeModuleException        if the given {@code instance} is not an instance of the expected {@code class}
   * @throws InvocationModuleException       if an error occurs during the execution of the method
   */
  @Throws(JavaInvokeErrorProvider.class)
  @OutputResolver(output = InstanceMethodTypeResolver.class)
  @Execution(CPU_INTENSIVE)
  public Result<Object, Void> invoke(
                                     @ParameterGroup(
                                         name = "Method") @MetadataKeyId(InstanceMethodTypeResolver.class) MethodIdentifier identifier,
                                     Object instance,
                                     @Optional @NullSafe @Content @TypeResolver(InstanceMethodTypeResolver.class) Map<String, TypedValue<Object>> args,
                                     @Optional @Placement(
                                         tab = MIME_TYPE_TAB) @Summary("The mime type of the payload that this invocation will output") String outputMimeType,
                                     @Optional @Placement(
                                         tab = MIME_TYPE_TAB) @Summary("The encoding of the payload that this invocation will output") String outputEncoding,
                                     @DefaultEncoding String defaultEncoding)
      throws ClassNotFoundModuleException, WrongTypeModuleException, ArgumentMismatchModuleException,
      InvocationModuleException, NoSuchMethodModuleException {

    validateType(identifier.getClazz(), instance, true, cache);
    Method method = cache.getMethod(identifier, instance.getClass(), args, false);
    return invokeMethod(method, args, instance, identifier, outputMimeType, outputEncoding,
                        transformationService, expressionManager, LOGGER, defaultEncoding);
  }

}
