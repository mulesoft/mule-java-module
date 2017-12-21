/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
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


/**
 * JavaModule MuleSoft Extension, used to invoke Java code.
 *
 * @since 1.0
 */
@Extension(name = "Java")
@Operations({JavaNewInstanceOperation.class, JavaInvokeOperations.class, JavaModuleValidateOperation.class})
@ExpressionFunctions(JavaModuleFunctions.class)
@ErrorTypes(JavaModuleError.class)
public class JavaModule {

}
