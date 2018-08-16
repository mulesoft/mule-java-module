/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.internal.metadata;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import org.mule.runtime.api.metadata.MetadataKey;

import java.util.List;
import java.util.Set;

import org.junit.Test;

public class KeysMetadataTestCase extends AbstractMetadataTestCase {

  @Test
  public void instanceMethodKeys() throws Exception {

    Set<MetadataKey> keys = getKeys(flow(INVOKE, EXECUTABLE_ELEMENT), INSTANCE_CATEGORY);
    assertThat(keys.iterator().next().getChilds().size(), is(21));
    assertThat(getIds(keys), containsInAnyOrder(
                                                "getPhase()", "getPhaseId()", "equals(java.lang.Object)", "sayHi(int)",
                                                "notify()", "sayHi()", "hashCode()", "addToList(java.util.List)",
                                                "createEmptyPojo()", "notifyAll()", "sayHi(java.lang.String,int)", "wait()",
                                                "addToMap(java.util.Map)",
                                                "addToList(java.util.ArrayList)", "nextPhase()", "wait(long)", "getClass()",
                                                "wait(long,int)", "addToList(java.util.LinkedList)", "sayHi(java.lang.String)",
                                                "toString()"));
    assertThat(getDisplayNames(keys), containsInAnyOrder("getPhase()", "getPhaseId()", "equals(Object arg0)",
                                                         "sayHi(int id)", "notify()", "sayHi()",
                                                         "hashCode()", "addToList(List list)", "createEmptyPojo()",
                                                         "notifyAll()", "sayHi(String name, int id)", "wait()",
                                                         "addToMap(Map input)", "addToList(ArrayList list)",
                                                         "nextPhase()", "wait(long arg0)", "getClass()",
                                                         "wait(long arg0, int arg1)", "addToList(LinkedList linkedList)",
                                                         "sayHi(String name)", "toString()"));
  }

  @Test
  public void staticMethodKeys() throws Exception {
    Set<MetadataKey> keys = getKeys(flow(INVOKE_STATIC, EXECUTABLE_ELEMENT, CREATE + PHASE), STATIC_CATEGORY);
    assertThat(getIds(keys),
               containsInAnyOrder("create(org.mule.extensions.internal.model.ExecutableElement.Phase)", "create()", "className()",
                                  "throwException(java.lang.String)", "getNull()",
                                  "makeCatSaySomething(org.mule.extensions.internal.model.robot.Cat)",
                                  "makeCatSaySomething(org.mule.extensions.internal.model.real.Cat)",
                                  "makeCatsSaySomething(org.mule.extensions.internal.model.robot.Cat,org.mule.extensions.internal.model.real.Cat)"));
    assertThat(getDisplayNames(keys), containsInAnyOrder("create(Phase initPhase)", "create()", "className()",
                                                         "throwException(String message)", "getNull()",
                                                         "makeCatSaySomething(org.mule.extensions.internal.model.robot.Cat cat)",
                                                         "makeCatSaySomething(org.mule.extensions.internal.model.real.Cat cat)",
                                                         "makeCatsSaySomething(org.mule.extensions.internal.model.robot.Cat robotCat, org.mule.extensions.internal.model.real.Cat realCat)"));
  }

  @Test
  public void constructorKeys() throws Exception {
    Set<MetadataKey> keys = getKeys(flow(NEW, COMPOSITE_POJO, COMPOSITE_POJO), CONSTRUCTOR_CATEGORY);
    assertThat(keys.iterator().next().getChilds().size(), is(4));
    assertThat(getIds(keys), containsInAnyOrder("CompositePojo(java.lang.String,java.util.Map)",
                                                "CompositePojo(org.mule.extensions.internal.model.CompositePojo)",
                                                "CompositePojo(java.lang.String)", "CompositePojo()"));
    assertThat(getDisplayNames(keys), containsInAnyOrder("CompositePojo(String name, Map childs)",
                                                         "CompositePojo(CompositePojo child)",
                                                         "CompositePojo(String name)",
                                                         "CompositePojo()"));

    keys = getKeys(flow(NEW, EXECUTABLE_ELEMENT), CONSTRUCTOR_CATEGORY);
    assertThat(keys.iterator().next().getChilds().size(), is(3));
    assertThat(getIds(keys), containsInAnyOrder("ExecutableElement(org.mule.extensions.internal.model.ExecutableElement.Phase)",
                                                "ExecutableElement()", "ExecutableElement(java.lang.String)"));
    assertThat(getDisplayNames(keys), containsInAnyOrder("ExecutableElement(Phase initPhase)", "ExecutableElement()",
                                                         "ExecutableElement(String errorMessage)"));
  }

  private List<String> getIds(Set<MetadataKey> keys) {
    return keys.stream().flatMap(k -> k.getChilds().stream())
        .map(MetadataKey::getId)
        .collect(toList());
  }

  private List<String> getDisplayNames(Set<MetadataKey> keys) {
    return keys.stream().flatMap(k -> k.getChilds().stream())
        .map(MetadataKey::getDisplayName)
        .collect(toList());
  }
}
