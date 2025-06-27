/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.api.exception;

import static org.mule.extensions.java.api.error.JavaModuleError.NOT_INSTANTIABLE_TYPE;
import static org.mule.extensions.java.internal.util.JavaModuleUtils.getArgumentsMessage;
import static org.mule.extensions.java.internal.util.JavaModuleUtils.getCauseMessage;
import static org.mule.extensions.java.internal.util.JavaModuleUtils.toHumanReadableArgs;

import org.mule.extensions.java.api.error.JavaModuleError;
import org.mule.extensions.java.internal.parameters.ExecutableIdentifier;
import org.mule.runtime.api.metadata.TypedValue;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A {@link JavaModuleException} related with the {@link JavaModuleError#NOT_INSTANTIABLE_TYPE} Error type
 *
 * @since 1.0
 */
public class NonInstantiableTypeModuleException extends JavaModuleException {

  public NonInstantiableTypeModuleException(ExecutableIdentifier id,
                                            Map<String, TypedValue<Object>> args,
                                            Throwable cause) {
    super(buildMessage(id, toHumanReadableArgs(args), getCauseMessage(cause)), NOT_INSTANTIABLE_TYPE, cause);
  }

  private static String buildMessage(ExecutableIdentifier identifier, List<String> args, Optional<String> cause) {
    StringBuilder sb = new StringBuilder()
        .append("Failed to instantiate Class '").append(identifier.getClazz())
        .append("' using the Constructor '").append(identifier.getElementId())
        .append("' ").append(getArgumentsMessage(args));

    cause.ifPresent(causeMessage -> sb.append(".\nCause: ").append(causeMessage));

    return sb.toString();
  }

}
