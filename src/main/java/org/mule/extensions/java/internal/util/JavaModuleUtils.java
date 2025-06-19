/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.internal.util;

import static org.mule.runtime.core.api.util.ClassUtils.isInstance;

import static java.lang.String.format;
import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import org.mule.extensions.java.api.exception.ClassNotFoundModuleException;
import org.mule.extensions.java.api.exception.WrongTypeModuleException;
import org.mule.extensions.java.internal.cache.JavaModuleLoadingCache;
import org.mule.extensions.java.internal.parameters.ExecutableIdentifier;
import org.mule.extensions.java.internal.transformer.ParameterTransformer;
import org.mule.extensions.java.internal.transformer.ParametersTransformationResult;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.transformation.TransformationService;
import org.mule.runtime.core.api.el.ExpressionManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.slf4j.Logger;
import org.springframework.core.ResolvableType;

/**
 * Utility class to common functions across operations
 *
 * @since 1.0.0
 */
public final class JavaModuleUtils {

  public static final String ARG_0 = "arg0";
  public static final String ARG_PREFIX = "arg";

  private JavaModuleUtils() {}

  public static void validateType(String clazz, Object instance, boolean acceptSubtypes, JavaModuleLoadingCache cache)
      throws ClassNotFoundModuleException, WrongTypeModuleException {

    Class<?> declaredClass = cache.loadClass(clazz);

    boolean isValid = acceptSubtypes
        ? isInstance(declaredClass, instance)
        : declaredClass.equals(instance.getClass());

    if (!isValid) {
      throw new WrongTypeModuleException(clazz, instance.getClass().getName());
    }
  }

  public static void logTooManyArgsWarning(Executable method, Map<String, TypedValue<Object>> args,
                                           ExecutableIdentifier identifier,
                                           Logger logger) {
    String expectedArgs = method.getParameters().length == 0
        ? "No arguments were expected"
        : "Expected arguments are " + toHumanReadableArgs(method);

    logger.warn(format("Too many arguments were provided for the invocation of %s '%s' from Class '%s'."
        + " %s but got %s.",
                       identifier.getExecutableTypeName(), identifier.getElementId(), identifier.getClazz(),
                       expectedArgs, toHumanReadableArgs(args)));
  }

  public static ParametersTransformationResult getSortedAndTransformedArgs(Map<String, TypedValue<Object>> args,
                                                                           Executable executable,
                                                                           TransformationService transformationService,
                                                                           ExpressionManager expressionManager, Logger logger) {
    Parameter[] parameters = executable.getParameters();
    if (parameters.length == 0) {
      return new ParametersTransformationResult(emptyList(), emptyList(), emptyList());
    }

    boolean useCanonicalArgName = args.containsKey(ARG_0);
    ParameterTransformer parameterTransformer = new ParameterTransformer(executable, transformationService, expressionManager);
    List<Object> sortedArgs = new ArrayList<>(parameters.length);
    List<String> missingArgs = new LinkedList<>();
    List<String> failedToTransformArgs = new LinkedList<>();

    for (int i = 0; i < parameters.length; i++) {
      Parameter parameter = parameters[i];
      try {
        TypedValue<Object> value = useCanonicalArgName ? args.get(ARG_PREFIX + i) : args.get(parameter.getName());
        if (value == null) {
          missingArgs.add(parameter.getName());
          continue;
        }
        Object originalValue = value.getValue();
        if (parameterTransformer.parameterNeedsTransformation(originalValue, i)) {
          Optional<Object> transformedValue = parameterTransformer.transformParameter(originalValue, i);
          if (transformedValue.isPresent()) {
            sortedArgs.add(transformedValue.get());
          } else {
            failedToTransformArgs.add(parameter.getName());
          }
        } else {
          sortedArgs.add(originalValue);
        }
      } catch (Exception e) {
        if (logger.isDebugEnabled()) {
          logger.debug("An unexpected error occurred while transforming the parameter '" + parameter.getName() + "'", e);
        }
        failedToTransformArgs.add(parameter.getName());
      }
    }

    return new ParametersTransformationResult(sortedArgs, failedToTransformArgs, missingArgs);
  }

  public static List<String> toHumanReadableArgs(Map<String, TypedValue<Object>> args) {
    return args.entrySet().stream()
        .map(e -> ResolvableType.forType(e.getValue().getDataType().getType()).toString() + " " + e.getKey())
        .collect(toList());
  }

  public static List<String> toHumanReadableArgs(List<Object> args) {
    return args.stream().map(t -> t != null ? ResolvableType.forType(t.getClass()).toString() : "null").collect(toList());
  }

  public static List<String> toHumanReadableArgs(Executable executable) {
    Function<Integer, ResolvableType> typeResolver = executable instanceof Method method
        ? i -> ResolvableType.forMethodParameter(method, i)
        : i -> ResolvableType.forConstructorParameter((Constructor<?>) executable, i);

    Parameter[] args = executable.getParameters();
    List<String> typeNames = new ArrayList<>(args.length);
    for (int i = 0; i < args.length; i++) {
      typeNames.add(typeResolver.apply(i).toString() + " " + args[i].getName());
    }
    return typeNames;
  }

  public static Optional<String> getCauseMessage(Throwable cause) {
    if (cause != null) {
      if (cause.getMessage() != null && !cause.getMessage().trim().isEmpty()) {
        return Optional.of(cause.getClass().getName() + " - " + cause.getMessage());
      }
      return getCauseMessage(cause.getCause());
    }
    return Optional.empty();
  }

  public static String getArgumentsMessage(List<String> args) {
    if (args.isEmpty()) {
      return "without any argument";
    }

    return "with arguments " + args;
  }

  /**
   * Method to obtain the Public Methods for a given class.
   * <p>
   * Always filters the non Bridge Methods. For more information about Bridge Methods see
   * https://docs.oracle.com/javase/tutorial/java/generics/bridgeMethods.html
   * </p>
   *
   * @param clazz        the Java class for which we want its Public methods.
   * @param expectStatic true to get only static methods, false to get only non static methods.
   * @return List of Methods for the class.
   */
  public static List<Method> getPublicMethods(Class<?> clazz, boolean expectStatic) {
    return stream(clazz.getMethods())
        .filter(m -> isPublic(m.getModifiers()))
        .filter(m -> expectStatic == isStatic(m.getModifiers()))
        .filter(m -> !m.isBridge())
        .collect(toList());
  }

}
