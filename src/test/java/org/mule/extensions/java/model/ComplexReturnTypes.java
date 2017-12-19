/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.model;

import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.api.metadata.TypedValue;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class ComplexReturnTypes {

  public Function<Integer, List<Integer>> getFunction() {
    return Collections::singletonList;
  }

  public Optional<Boolean> tripleStateBoolean(boolean isEmpty) {
    return isEmpty ? Optional.empty() : Optional.of(false);
  }

  public TypedValue<String> getTypedValue(String xml) {
    return new TypedValue<>(xml, DataType.XML_STRING);
  }

}
