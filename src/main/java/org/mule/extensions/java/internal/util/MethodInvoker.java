/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.internal.util;

import static org.mule.extensions.java.internal.util.JavaModuleUtils.logTooManyArgsWarning;

import org.mule.extensions.java.api.exception.ArgumentMismatchModuleException;
import org.mule.extensions.java.api.exception.InvocationModuleException;
import org.mule.extensions.java.api.exception.NoSuchMethodModuleException;
import org.mule.extensions.java.internal.parameters.ExecutableIdentifier;
import org.mule.extensions.java.internal.transformer.ParametersTransformationResult;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.api.metadata.MediaTypeUtils;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.transformation.TransformationService;
import org.mule.runtime.core.api.el.ExpressionManager;
import org.mule.runtime.extension.api.runtime.operation.Result;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;

/**
 * Utility class to common functions across operations
 *
 * @since 1.0
 */
public final class MethodInvoker {

  private MethodInvoker() {}


  public static Result<Object, Void> invokeMethod(Method method, Map<String, TypedValue<Object>> args,
                                                  Object instance, ExecutableIdentifier identifier,
                                                  String outputMimeType, String outputEncoding,
                                                  TransformationService transformationService,
                                                  ExpressionManager expressionManager,
                                                  Logger logger, String defaultEncoding)
      throws ArgumentMismatchModuleException, InvocationModuleException, NoSuchMethodModuleException {

    Object payload = invokeMethod(method, args, instance, identifier, transformationService, expressionManager, logger);
    Result.Builder<Object, Void> output = Result.<Object, Void>builder().output(payload);
    Optional<MediaType> mediaType = getMediaType(outputMimeType);
    Optional<Charset> encoding = getEncoding(outputEncoding);

    if (mediaType.isPresent() || encoding.isPresent()) {
      MediaType outputMediaType = mediaType.orElse(MediaType.ANY);
      Charset outputCharset = encoding.orElseGet(() -> Charset.forName(defaultEncoding));
      output.mediaType(outputMediaType.withCharset(outputCharset));
    }
    return output.build();
  }


  public static Object invokeMethod(Method method, Map<String, TypedValue<Object>> args,
                                    Object instance, ExecutableIdentifier identifier,
                                    TransformationService transformationService,
                                    ExpressionManager expressionManager,
                                    Logger logger)
      throws ArgumentMismatchModuleException, InvocationModuleException, NoSuchMethodModuleException {

    ParametersTransformationResult transformationResult =
        JavaModuleUtils.getSortedAndTransformedArgs(args, method, transformationService, expressionManager, logger);

    if (method.getParameters().length > args.size()) {
      throw new ArgumentMismatchModuleException(
                                                "Failed to invoke %s '%s' from Class '%s'. Too few arguments were provided for the invocation"
                                                    .formatted(
                                                               identifier.getExecutableTypeName(), identifier.getElementId(),
                                                               identifier.getClazz()),
                                                method, args, transformationResult);

    } else if (method.getParameters().length < args.size()) {
      logTooManyArgsWarning(method, args, identifier, logger);
    }

    if (transformationResult.isSuccess()) {
      return doInvoke(method, args, instance, identifier, transformationResult);
    }

    throw new ArgumentMismatchModuleException("Failed to invoke %s '%s' from Class '%s'. The given arguments could not be transformed to match those expected by the %s"
        .formatted(
                   identifier.getExecutableTypeName(), identifier.getElementId(),
                   identifier.getClazz(), identifier.getExecutableTypeName()),
                                              method, args, transformationResult);

  }

  private static Object doInvoke(Method method, Map<String, TypedValue<Object>> args, Object instance,
                                 ExecutableIdentifier identifier, ParametersTransformationResult transformationResult) {
    try {
      return method.invoke(instance, transformationResult.getTransformed().toArray());

    } catch (IllegalArgumentException e) {
      throw new ArgumentMismatchModuleException("Failed to invoke %s '%s' from Class '%s'".formatted(
                                                                                                     identifier
                                                                                                         .getExecutableTypeName(),
                                                                                                     identifier.getElementId(),
                                                                                                     identifier.getClazz()),
                                                method, args, transformationResult, e);

    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new InvocationModuleException("%s '%s' from Class '%s' ".formatted(
                                                                               identifier.getExecutableTypeName(),
                                                                               identifier.getElementId(),
                                                                               identifier.getClazz()),
                                          method, args, e);
    }
  }

  private static Optional<MediaType> getMediaType(String outputMimeType) {
    try {
      return outputMimeType == null || outputMimeType.trim().isEmpty()
          ? Optional.empty()
          : Optional.of(MediaType.parse(outputMimeType));
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("The 'outputMimeType' specified is not a valid MediaType - " + e.getMessage(), e);
    }
  }

  private static Optional<Charset> getEncoding(String outputEncoding) {
    try {
      return outputEncoding == null || outputEncoding.trim().isEmpty()
          ? Optional.empty()
          : Optional.of(MediaTypeUtils.parseCharset(outputEncoding));
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("The 'outputEncoding' specified is not a valid Charset - " + e.getMessage(), e);
    }
  }


}
