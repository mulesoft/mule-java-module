/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.api.exception;

import static org.mule.extensions.java.api.error.JavaModuleError.INVOCATION;
import static org.mule.extensions.java.internal.JavaModuleUtils.getArgumentsMessage;
import static org.mule.extensions.java.internal.JavaModuleUtils.getCauseMessage;
import static org.mule.extensions.java.internal.JavaModuleUtils.toHumanReadableArgs;
import org.mule.extensions.java.api.error.JavaModuleError;
import org.mule.runtime.api.metadata.TypedValue;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A {@link JavaModuleException} related with the {@link JavaModuleError#INVOCATION} Error type
 *
 * @since 1.0
 */
public class InvocationModuleException extends JavaModuleException {

  public InvocationModuleException(String componentDescription, Map<String, TypedValue<Object>> args, Throwable cause) {
    super(buildMessage(componentDescription, toHumanReadableArgs(args), getCauseMessage(cause)), INVOCATION, cause);
  }

  private static String buildMessage(String componentDescription, List<String> args, Optional<String> cause) {
    StringBuilder sb = new StringBuilder()
        .append("Invocation of ")
        .append(componentDescription)
        .append(getArgumentsMessage(args))
        .append(" resulted in an error");

    cause.ifPresent(causeMessage -> sb.append(": ").append(causeMessage));

    return sb.toString();
  }

}
