/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.internal.transformer;

import static java.util.Collections.unmodifiableList;

import java.util.List;

/**
 * Contains all the information regarding how a set of input arguments were transformed to the expected parameters of an
 * Executable element
 * 
 * @since 1.1.2, 1.2.0
 */
public class ParametersTransformationResult {

  private boolean success;
  private List<Object> transformed;
  private List<String> failedToTransform;
  private List<String> missing;

  public ParametersTransformationResult(List<Object> transformed, List<String> failedToTransform, List<String> missing) {
    this.success = missing.isEmpty() && failedToTransform.isEmpty();
    this.transformed = transformed;
    this.missing = missing;
    this.failedToTransform = failedToTransform;
  }

  public boolean isSuccess() {
    return success;
  }

  public List<Object> getTransformed() {
    return unmodifiableList(transformed);
  }

  public List<String> getFailedToTransform() {
    return unmodifiableList(failedToTransform);
  }

  public List<String> getMissing() {
    return unmodifiableList(missing);
  }

}
