/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.internal.model;

public class Car {

  private String doors;
  private String wheels;
  private String windows;
  private String engine;

  public Car() {}

  public Car(String doors, String wheels, String windows, String engine) {
    this.doors = doors;
    this.wheels = wheels;
    this.windows = windows;
    this.engine = engine;
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
