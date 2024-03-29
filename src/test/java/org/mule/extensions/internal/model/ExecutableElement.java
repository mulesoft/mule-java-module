/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.internal.model;

import org.mule.extensions.internal.model.robot.Cat;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ExecutableElement {

  public static final String ENRICH_KEY = "MY_PROP";
  public static final String ENRICH_VALUE = "ENRICHED";

  private Phase phase;

  public static ExecutableElement create() {
    return new ExecutableElement();
  }

  public static ExecutableElement create(Phase initPhase) {
    return new ExecutableElement(initPhase);
  }

  public static ExecutableElement create(Object phase) {
    return new ExecutableElement((Phase) phase);
  }

  public static Object getNull() {
    return null;
  }

  public static Object throwException(String message) {
    throw new RuntimeException(new CustomIllegalArgumentException(new NullPointerException(message)));
  }

  public ExecutableElement() {
    this.phase = Phase.NOT_STARTED;
  }

  public ExecutableElement(Phase initPhase) {
    this.phase = initPhase;
  }

  public ExecutableElement(String message) {
    throw new CustomIllegalArgumentException(message);
  }

  public String sayHi() {
    return "Hi";
  }

  public String sayHi(String name) {
    return "Hi " + name;
  }

  public String sayHi(int id) {
    return "Hi " + id;
  }

  public String sayHi(String name, int id) {
    return "Hi " + name + "::" + id;
  }

  public String sayHi(Optional<String> name) {
    return "Hi " + name.orElse("there");
  }

  public String sayHi(Optional<String> name, String lasname) {
    return "Hi " + name.orElse("there") + " " + lasname;
  }

  public String adopt(Optional<Cat> cat) {
    return cat.map(c -> "I love you " + c.getName()).orElse("I am better on my own.");
  }

  public ComplexReturnTypes createEmptyPojo() {
    return new ComplexReturnTypes();
  }

  public void nextPhase() {
    this.phase = phase.equals(Phase.NOT_STARTED) ? Phase.STARTED : Phase.STOPPED;
  }

  public Map<String, Object> addToMap(Map<String, Object> input) {
    input.put(ENRICH_KEY, ENRICH_VALUE);
    return input;
  }

  public Phase getPhase() {
    return phase;
  }

  public int getPhaseId() {
    return phase.ordinal();
  }

  public List<String> addToList(List<String> list) {
    list.add("List");
    return list;
  }

  public List<String> addToList(ArrayList<String> list) {
    list.add("ArrayList");
    return list;
  }

  public List<String> addToList(LinkedList<String> linkedList) {
    linkedList.add("LinkedList");
    return linkedList;
  }

  public static String className() {
    return ExecutableElement.class.getName();
  }

  public static String makeCatSaySomething(org.mule.extensions.internal.model.real.Cat cat) {
    return cat.saySomething();
  }

  public static String makeCatSaySomething(org.mule.extensions.internal.model.robot.Cat cat) {
    return cat.saySomething();
  }

  public static String makeCatsSaySomething(org.mule.extensions.internal.model.robot.Cat robotCat,
                                            org.mule.extensions.internal.model.real.Cat realCat) {
    return robotCat.saySomething() + " " + realCat.saySomething();
  }

  protected void protectedMethod() {}

  private Object privateMethod() {
    return null;
  }

  private static int privateStaticMethod() {
    return 0;
  }

  public enum Phase {
    NOT_STARTED, STARTED, STOPPED
  }
}
