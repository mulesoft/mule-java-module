/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.java.internal;

import org.mule.extensions.java.api.error.JavaModuleError;
import org.mule.extensions.java.internal.function.JavaModuleFunctions;
import org.mule.extensions.java.internal.operation.JavaInvokeOperations;
import org.mule.extensions.java.internal.operation.JavaModuleValidateOperation;
import org.mule.extensions.java.internal.operation.JavaNewInstanceOperation;
import org.mule.runtime.extension.api.annotation.ExpressionFunctions;
import org.mule.runtime.extension.api.annotation.Extension;
import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.error.ErrorTypes;
import org.mule.sdk.api.annotation.JavaVersionSupport;

import static org.mule.sdk.api.meta.JavaVersion.JAVA_11;
import static org.mule.sdk.api.meta.JavaVersion.JAVA_17;
import static org.mule.sdk.api.meta.JavaVersion.JAVA_8;


/**
 * JavaModule MuleSoft Extension, used to invoke Java code.
 *
 * @since 1.0
 */
@Extension(name = "Java")
@Operations({JavaNewInstanceOperation.class, JavaInvokeOperations.class, JavaModuleValidateOperation.class})
@ExpressionFunctions(JavaModuleFunctions.class)
@ErrorTypes(JavaModuleError.class)
@JavaVersionSupport({JAVA_8, JAVA_11, JAVA_17})
public class JavaModule {

  public static final String APPLICATION_JAVA = "application/java";

}
