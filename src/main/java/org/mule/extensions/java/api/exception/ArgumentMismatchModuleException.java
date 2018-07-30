/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.api.exception;

import static java.util.Arrays.stream;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;
import static org.mule.extensions.java.api.error.JavaModuleError.ARGUMENTS_MISMATCH;
import static org.mule.extensions.java.internal.util.JavaModuleUtils.getArgumentsMessage;
import static org.mule.extensions.java.internal.util.JavaModuleUtils.getCauseMessage;
import static org.mule.extensions.java.internal.util.JavaModuleUtils.toHumanReadableArgs;
import org.mule.extensions.java.api.error.JavaModuleError;
import org.mule.extensions.java.internal.transformer.ParametersTransformationResult;
import org.mule.runtime.api.metadata.TypedValue;

import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A {@link JavaModuleException} related with the {@link JavaModuleError#ARGUMENTS_MISMATCH} Error type
 *
 * @since 1.0
 */
public class ArgumentMismatchModuleException extends JavaModuleException {

  public ArgumentMismatchModuleException(String failure, Executable executable,
                                         Map<String, TypedValue<Object>> args,
                                         ParametersTransformationResult transformationResult) {
    super(buildMessage(failure, executable, args, transformationResult, empty()), ARGUMENTS_MISMATCH);
  }

  public ArgumentMismatchModuleException(String failure, Executable executable,
                                         Map<String, TypedValue<Object>> args,
                                         ParametersTransformationResult transformationResult,
                                         Throwable cause) {
    super(buildMessage(failure, executable, args, transformationResult, getCauseMessage(cause)),
          ARGUMENTS_MISMATCH, cause);
  }

  private static String buildMessage(String failure, Executable executable,
                                     Map<String, TypedValue<Object>> args,
                                     ParametersTransformationResult transformationResult,
                                     Optional<String> cause) {
    StringBuilder sb = new StringBuilder()
        .append(failure).append(". ");

    Parameter[] parameters = executable.getParameters();
    if (parameters.length > 0) {
      sb.append("\nExpected arguments are ")
          .append(toHumanReadableArgs(executable))
          .append(" and invocation was attempted ")
          .append(getArgumentsMessage(toHumanReadableArgs(args)));
    } else {
      sb.append("\nNo arguments were expected and invocation was attempted ")
          .append(getArgumentsMessage(toHumanReadableArgs(args)));
    }

    if (!args.isEmpty()) {
      logMissing(transformationResult, sb);

      logNotTransformed(transformationResult, sb);

      logInvalidNullValues(args, sb, parameters);
    }

    sb.append(cause.map(causeMessage -> ": " + causeMessage).orElse("."));

    return sb.toString();
  }

  private static void logInvalidNullValues(Map<String, TypedValue<Object>> args, StringBuilder sb, Parameter[] parameters) {
    List<String> nullPrimitives = stream(parameters)
        .filter(p -> p.getType().isPrimitive())
        .filter(p -> args.get(p.getName()) != null && args.get(p.getName()).getValue() == null)
        .map(Parameter::getName)
        .collect(toList());

    if (!nullPrimitives.isEmpty()) {
      sb.append(".\n");
      if (nullPrimitives.size() == 1) {
        sb.append("Parameter '").append(nullPrimitives.get(0)).append("'")
            .append(" cannot be assigned with null, but a null value was provided");
      } else {
        sb.append("Parameters ")
            .append(nullPrimitives)
            .append(" cannot be assigned with null, but null values were provided");
      }
    }
  }

  private static void logNotTransformed(ParametersTransformationResult transformationResult, StringBuilder sb) {
    if (!transformationResult.getFailedToTransform().isEmpty()) {
      sb.append(".\nNo suitable transformation was found to match the expected type for the parameter");
      if (transformationResult.getFailedToTransform().size() > 1) {
        sb.append("s");
      }
      sb.append(" ")
          .append(transformationResult.getFailedToTransform());
    }
  }

  private static void logMissing(ParametersTransformationResult transformationResult, StringBuilder sb) {
    if (!transformationResult.getMissing().isEmpty()) {
      sb.append(".\nMissing parameter");
      if (transformationResult.getMissing().size() > 1) {
        sb.append("s are");
      }

      sb.append(" ").append(transformationResult.getMissing());
    }
  }

}
