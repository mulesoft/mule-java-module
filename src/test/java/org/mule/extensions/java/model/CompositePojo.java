/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.model;

import static java.util.Collections.singletonList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompositePojo {

  private String name;

  private Map<String, List<CompositePojo>> childs = new HashMap<>();

  public CompositePojo() {}

  public CompositePojo(String name) {
    this.name = name;
  }

  public CompositePojo(CompositePojo child) {
    childs.put(child.getName(), singletonList(child));
  }

  public CompositePojo(Map<String, List<CompositePojo>> childs) {
    this.childs.putAll(childs);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Map<String, List<CompositePojo>> getChilds() {
    return childs;
  }

  public void setChilds(Map<String, List<CompositePojo>> childs) {
    this.childs = childs;
  }

}
