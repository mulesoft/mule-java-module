/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.internal.model;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class CarDummyTest {

  private String doors;
  private String wheels;
  private String windows;
  private String engine;

  public CarDummyTest() {}

  public CarDummyTest(String doors, String wheels, String windows, String engine) {
    this.doors = doors;
    this.wheels = wheels;
    this.windows = windows;
    this.engine = engine;
  }

  public Map<String, Object> asMap() {
    ImmutableMap.Builder<String, Object> builder = new ImmutableMap.Builder<>();

    addIfNotNullValue(builder, "doors", this.doors);
    addIfNotNullValue(builder, "wheels", this.wheels);
    addIfNotNullValue(builder, "windows", this.windows);
    addIfNotNullValue(builder, "engine", this.engine);

    return builder.build();
  }

  private void addIfNotNullValue(ImmutableMap.Builder<String, Object> builder, String key, Object value) {
    if (value != null) {
      builder.put(key, value);
    }
  }

  public void setDoors(String doors) {
    this.doors = doors;
  }

  public void setWheels(String wheels) {
    this.wheels = wheels;
  }

  public void setWindows(String windows) {
    this.windows = windows;
  }

  public void setEngine(String engine) {
    this.engine = engine;
  }

  public String getDoors() {
    return doors;
  }

  public String getWheels() {
    return wheels;
  }

  public String getWindows() {
    return windows;
  }

  public String getEngine() {
    return engine;
  }
}
