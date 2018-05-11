/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.internal.model;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnotherExecutableElement {

  public static String getSameWord(String word) {
    return word;
  }

  public static String className() {
    return AnotherExecutableElement.class.getName();
  }

  public static String getNumber(String number) {
    return number;
  }

  public static String sayInputStream(InputStream inputStream) throws Exception {
    byte[] input = new byte[50];
    int bytesRead = inputStream.read(input);
    return new String(input).substring(0, bytesRead);
  }

  public static Integer getMapEntry(Map<String, Integer> map, String key) {
    return map.get(key);
  }

  public static String sayItemFromMapEntry(Map<String, List<String>> map, String key, Integer index) {
    return map.get(key).get(index);
  }

  public static String sayItemFromSpecificListImplementation(ArrayList<String> list, Integer index) {
    return list.get(index);
  }

  public static String sayValueFromSpecificMapImplementation(HashMap<String, String> map, InputStream inputStream)
      throws Exception {
    byte[] input = new byte[50];
    int bytesRead = inputStream.read(input);
    return new String(map.get(new String(input))).substring(0, bytesRead);
  }

  public static String getFirstItemFromNestedList(List<List<List<List<String>>>> nestedLists) {
    return nestedLists.get(0).get(0).get(0).get(0);
  }

  public static String getCarDoors(Car car) {
    return car.getDoors();
  }

  public static Car getCarFromList(List<Car> cars, int index) {
    return cars.get(index);
  }

  public static Integer getFromMapInsideListAndSumFour(List<Map<String, Integer>> listOfMaps, int index, String key) {
    return listOfMaps.get(index).get(key) + 4;
  }

  public static Class<?> getClassFromFirstItem(List<? extends String> items) {
    return items.get(0).getClass();
  }

}
