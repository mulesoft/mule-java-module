/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.internal.operation;

import static java.lang.String.format;
import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Arrays.stream;
import org.mule.extensions.java.api.exception.ArgumentMismatchModuleException;
import org.mule.extensions.java.api.exception.InvocationModuleException;
import org.mule.extensions.java.api.exception.NoSuchMethodModuleException;
import org.mule.extensions.java.api.exception.WrongTypeModuleException;
import org.mule.extensions.java.internal.JavaModule;
import org.mule.extensions.java.internal.error.JavaInvokeErrorProvider;
import org.mule.extensions.java.internal.metadata.InstanceMethodTypeResolver;
import org.mule.extensions.java.internal.metadata.StaticMethodTypeResolver;
import org.mule.extensions.java.internal.parameters.ExecutableIdentifier;
import org.mule.extensions.java.internal.parameters.MethodIdentifier;
import org.mule.extensions.java.internal.parameters.StaticMethodIdentifier;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.core.api.util.ClassUtils;
import org.mule.runtime.extension.api.annotation.error.Throws;
import org.mule.runtime.extension.api.annotation.metadata.MetadataKeyId;
import org.mule.runtime.extension.api.annotation.metadata.OutputResolver;
import org.mule.runtime.extension.api.annotation.metadata.TypeResolver;
import org.mule.runtime.extension.api.annotation.param.Content;
import org.mule.runtime.extension.api.annotation.param.NullSafe;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Defines the operations of {@link JavaModule} related to invocation of class or instance methods using reflection.
 *
 * @since 1.0
 */
public class JavaInvokeOperations {

  @Throws(JavaInvokeErrorProvider.class)
  @OutputResolver(output = StaticMethodTypeResolver.class)
  public Object invokeStatic(
                             @ParameterGroup(
                                 name = "Method") @MetadataKeyId(StaticMethodTypeResolver.class) StaticMethodIdentifier identifier,
                             @Optional @NullSafe @Content @TypeResolver(StaticMethodTypeResolver.class) LinkedHashMap<String, TypedValue<Object>> args)
      throws ClassNotFoundException, WrongTypeModuleException, IllegalAccessException, NoSuchMethodModuleException,
      ArgumentMismatchModuleException, InvocationModuleException {

    Class<?> targetClass = ClassUtils.loadClass(identifier.getClazz(), getClass());

    List<Method> methods = stream(targetClass.getMethods())
        .filter(m -> isStatic(m.getModifiers()) && isPublic(m.getModifiers()))
        .collect(Collectors.toList());

    return invokeMethod(targetClass, null, identifier, methods, args);
  }

  @Throws(JavaInvokeErrorProvider.class)
  @OutputResolver(output = InstanceMethodTypeResolver.class)
  public Object invoke(
                       @ParameterGroup(
                           name = "Method") @MetadataKeyId(InstanceMethodTypeResolver.class) MethodIdentifier identifier,
                       Object instance,
                       @Optional @NullSafe @Content @TypeResolver(InstanceMethodTypeResolver.class) LinkedHashMap<String, TypedValue<Object>> args)
      throws ClassNotFoundException, WrongTypeModuleException, IllegalAccessException, NoSuchMethodModuleException,
      ArgumentMismatchModuleException, InvocationModuleException {

    JavaModuleUtils.validateType(identifier.getClazz(), instance, true);

    List<Method> methods = stream(instance.getClass().getMethods())
        .filter(m -> isPublic(m.getModifiers()) && !isStatic(m.getModifiers()))
        .collect(Collectors.toList());

    return invokeMethod(instance.getClass(), instance, identifier, methods, args);
  }

  private Object invokeMethod(Class<?> targetClass, Object instance,
                              ExecutableIdentifier identifier,
                              List<Method> methods,
                              LinkedHashMap<String, TypedValue<Object>> args)
      throws IllegalAccessException, NoSuchMethodModuleException, ArgumentMismatchModuleException,
      InvocationModuleException {

    Method method = methods.stream()
        .filter(identifier::matches)
        .findFirst()
        .orElseThrow(() -> new NoSuchMethodModuleException(identifier, targetClass, methods, args));

    try {
      List<Object> sortedArgs = JavaModuleUtils.getSortedArgs(args, method.getParameters());
      if (sortedArgs.size() != method.getParameters().length) {
        throw new ArgumentMismatchModuleException(failureMsg(identifier, method), method, args);
      }

      return method.invoke(instance, sortedArgs.toArray());

    } catch (IllegalArgumentException e) {
      throw new ArgumentMismatchModuleException(failureMsg(identifier, method), method, args, e);

    } catch (InvocationTargetException e) {
      throw new InvocationModuleException(failureMsg(identifier, method), args, e);
    }
  }

  private String failureMsg(ExecutableIdentifier identifier, Method method) {
    return format("Failed to invoke Method [%s] in Class [%s]",
                  identifier.getElementId(), method.getDeclaringClass().getName());
  }

}
