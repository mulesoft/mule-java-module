/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.internal.operation;

import static java.lang.String.format;
import static org.mule.extensions.java.internal.JavaModuleUtils.findMethod;
import static org.mule.extensions.java.internal.JavaModuleUtils.invokeMethod;
import org.mule.extensions.java.api.cache.JavaModuleLoadingCache;
import org.mule.extensions.java.api.exception.ArgumentMismatchModuleException;
import org.mule.extensions.java.api.exception.ClassNotFoundModuleException;
import org.mule.extensions.java.api.exception.InvocationModuleException;
import org.mule.extensions.java.api.exception.NoSuchMethodModuleException;
import org.mule.extensions.java.api.exception.WrongTypeModuleException;
import org.mule.extensions.java.internal.JavaModule;
import org.mule.extensions.java.internal.JavaModuleUtils;
import org.mule.extensions.java.internal.error.JavaInvokeErrorProvider;
import org.mule.extensions.java.internal.metadata.InstanceMethodTypeResolver;
import org.mule.extensions.java.internal.metadata.StaticMethodTypeResolver;
import org.mule.extensions.java.internal.parameters.ExecutableIdentifier;
import org.mule.extensions.java.internal.parameters.MethodIdentifier;
import org.mule.extensions.java.internal.parameters.StaticMethodIdentifier;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.extension.api.annotation.error.Throws;
import org.mule.runtime.extension.api.annotation.metadata.MetadataKeyId;
import org.mule.runtime.extension.api.annotation.metadata.OutputResolver;
import org.mule.runtime.extension.api.annotation.metadata.TypeResolver;
import org.mule.runtime.extension.api.annotation.param.Content;
import org.mule.runtime.extension.api.annotation.param.NullSafe;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;

import java.lang.reflect.Method;
import java.util.Map;

import javax.inject.Inject;

/**
 * Defines the operations of {@link JavaModule} related to invocation of class or instance methods using reflection.
 *
 * @since 1.0
 */
public class JavaInvokeOperations {

  @Inject
  private JavaModuleLoadingCache cache;

  /**
   * Operation that allows the user to invoke static methods with the provided arguments.
   * The identifier of the {@link Method} to be invoked includes the {@code class} and {@code method} names,
   * being the {@code method} a full description of its signature including the types of each parameter.
   * <p>
   * For example, if we want to invoke the static method {@code echo} with signature {@code public static String echo(String msg)}
   * which belongs to {@link Class} {@code org.bar.Foo}, then the identifier of the method will be {@code "echo(String)"}
   *
   * @param identifier the unique identifier for the method to be invoked.
   * @param args       the arguments used to invoke the given {@link Method}
   * @return the result of the {@link Method} invocation with the given {@code args}
   * @throws ClassNotFoundModuleException    if the given {@code class} is not found in the current context
   * @throws NoSuchMethodModuleException     if the given {@code class} does not declare a method with the given signature
   * @throws ArgumentMismatchModuleException if the {@code method} requires a different set of arguments than the ones provided
   * @throws InvocationModuleException       if an error occurs during the execution of the method
   */
  @Throws(JavaInvokeErrorProvider.class)
  @OutputResolver(output = StaticMethodTypeResolver.class)
  public Object invokeStatic(
                             @ParameterGroup(
                                 name = "Method") @MetadataKeyId(StaticMethodTypeResolver.class) StaticMethodIdentifier identifier,
                             @Optional @NullSafe @Content @TypeResolver(StaticMethodTypeResolver.class) Map<String, TypedValue<Object>> args)
      throws ClassNotFoundModuleException, ArgumentMismatchModuleException,
      InvocationModuleException, NoSuchMethodModuleException {

    Class<?> targetClass = cache.loadClass(identifier.getClazz())
        .orElseThrow(
                     () -> new ClassNotFoundModuleException(format("Failed to load Class with name [%s] while invoking static Method [%s]",
                                                                   identifier.getClazz(), identifier.getElementId())));

    Method method = findMethod(identifier, targetClass, true, args, cache);
    return invokeMethod(method, args, null, () -> failureMsg(identifier, method));
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
   * @param args       the arguments used to invoke the given {@link Method}
   * @return the result of the {@link Method} invocation with the given {@code args}
   * @throws ClassNotFoundModuleException    if the given {@code class} is not found in the current context
   * @throws NoSuchMethodModuleException     if the given {@code class} does not declare a method with the given signature
   * @throws ArgumentMismatchModuleException if the {@code method} requires a different set of arguments than the ones provided
   * @throws WrongTypeModuleException        if the given {@code instance} is not an instance of the expected {@code class}
   * @throws InvocationModuleException       if an error occurs during the execution of the method
   */
  @Throws(JavaInvokeErrorProvider.class)
  @OutputResolver(output = InstanceMethodTypeResolver.class)
  public Object invoke(
                       @ParameterGroup(
                           name = "Method") @MetadataKeyId(InstanceMethodTypeResolver.class) MethodIdentifier identifier,
                       Object instance,
                       @Optional @NullSafe @Content @TypeResolver(InstanceMethodTypeResolver.class) Map<String, TypedValue<Object>> args)
      throws ClassNotFoundModuleException, WrongTypeModuleException, ArgumentMismatchModuleException,
      InvocationModuleException, NoSuchMethodModuleException {

    JavaModuleUtils.validateType(identifier.getClazz(), instance, true, cache);

    Method method = findMethod(identifier, instance.getClass(), false, args, cache);
    return invokeMethod(method, args, instance, () -> failureMsg(identifier, method));
  }

  private String failureMsg(ExecutableIdentifier identifier, Method method) {
    return format("Failed to invoke Method [%s] in Class [%s]",
                  identifier.getElementId(), method.getDeclaringClass().getName());
  }

}
