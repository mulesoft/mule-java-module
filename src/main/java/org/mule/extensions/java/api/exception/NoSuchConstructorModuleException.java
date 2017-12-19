/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.api.exception;

import static java.util.stream.Collectors.toList;
import static org.mule.extensions.java.api.error.JavaModuleError.NO_SUCH_CONSTRUCTOR;
import org.mule.extensions.java.api.error.JavaModuleError;
import org.mule.extensions.java.internal.parameters.ExecutableIdentifier;
import org.mule.extensions.java.internal.parameters.ExecutableIdentifierFactory;
import org.mule.runtime.api.metadata.TypedValue;

import java.lang.reflect.Constructor;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * A {@link JavaModuleException} related with the {@link JavaModuleError#NO_SUCH_CONSTRUCTOR} Error type
 *
 * @since 1.0
 */
public class NoSuchConstructorModuleException extends JavaModuleException {

  public NoSuchConstructorModuleException(ExecutableIdentifier id, String targetClass,
                                          List<Constructor> constructors,
                                          LinkedHashMap<String, TypedValue<Object>> args) {
    super(buildMessage(id, targetClass, constructors, args), NO_SUCH_CONSTRUCTOR);
  }

  private static String buildMessage(ExecutableIdentifier id,
                                     String targetClass,
                                     List<Constructor> constructors,
                                     LinkedHashMap<String, TypedValue<Object>> args) {
    return String.format("No Constructor found with name [%s] and arguments %s in class [%s]. Available constructors are %s",
                         id.getElementId(), toHumanReadableArgs(args), targetClass,
                         constructors.stream().map(c -> ExecutableIdentifierFactory.create(c).getElementId()).collect(toList()));
  }

}
