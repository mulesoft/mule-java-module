/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.internal.function;

import static java.lang.String.format;
import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.mule.extensions.java.api.error.JavaModuleError.CLASS_NOT_FOUND;
import static org.mule.extensions.java.internal.operation.JavaModuleUtils.validateType;
import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static org.mule.runtime.core.api.util.ClassUtils.getMethod;
import static org.mule.runtime.core.api.util.ClassUtils.isInstance;
import static org.mule.runtime.core.api.util.ClassUtils.loadClass;
import org.mule.extensions.java.api.exception.ArgumentMismatchModuleException;
import org.mule.extensions.java.api.exception.InvocationModuleException;
import org.mule.extensions.java.api.exception.NoSuchMethodModuleException;
import org.mule.extensions.java.internal.JavaModule;
import org.mule.runtime.api.el.ExpressionFunction;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * The {@link ExpressionFunction}s exposed by the {@link JavaModule} that extends the EL
 * with further Java related functions.
 * 
 * @since 1.0
 */
public class JavaModuleFunctions {

  public Object invoke(@Alias("class") String clazz, @Alias("method") String methodName, Object instance,
                       @Optional List<Object> args)
      throws NoSuchMethodModuleException, InvocationModuleException, ClassNotFoundException {

    args = args == null ? emptyList() : args;
    final List<Class<?>> types = args.stream().map(Object::getClass).collect(toList());

    validateType(clazz, instance, true);

    Method method = getMethod(instance.getClass(), methodName, types.toArray(new Class[types.size()]));

    if (method == null) {
      List<Method> candidates = stream(instance.getClass().getMethods())
          .filter(m -> !isStatic(m.getModifiers()) && isPublic(m.getModifiers()))
          .collect(toList());

      throw new NoSuchMethodModuleException(methodName, instance.getClass(), candidates, args);
    }

    try {
      return method.invoke(instance, args.toArray());
    } catch (IllegalArgumentException e) {
      throw new ArgumentMismatchModuleException(format("Failed to invoke Method [%s] in Class [%s]", methodName, clazz),
                                                method, args, e);
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new InvocationModuleException(format("Failed to invoke Method [%s] in Class [%s]", methodName, clazz), args, e);
    }
  }

  public boolean isInstanceOf(Object instance, @Alias("class") String clazz) throws ModuleException {
    try {
      Class<?> declaredClass = loadClass(clazz, JavaModule.class);
      return isInstance(declaredClass, instance);
    } catch (ClassNotFoundException e) {
      throw new ModuleException(createStaticMessage("Failed to load class [%s]: ClassNotFoundException", clazz),
                                CLASS_NOT_FOUND, e);
    }
  }

}
