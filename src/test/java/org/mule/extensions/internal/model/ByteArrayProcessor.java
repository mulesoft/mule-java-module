/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.internal.model;

/**
 * Utility class for processing byte arrays
 */
public class ByteArrayProcessor {

  /**
   * Processes a byte array and returns a string representation
   * 
   * @param bytes The byte array to process
   * @return A string representation of the processed data
   */
  public static String processByteArray(byte[] bytes) {
    if (bytes == null) {
      return null;
    }
    return new String(bytes);
  }
}
