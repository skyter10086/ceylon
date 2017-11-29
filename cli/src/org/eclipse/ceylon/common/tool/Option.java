/********************************************************************************
 * Copyright (c) 2011-2017 Red Hat Inc. and/or its affiliates and others
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Apache License, Version 2.0 which is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: Apache-2.0 
 ********************************************************************************/
package org.eclipse.ceylon.common.tool;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a setter to be called with the value {@code true} if the 
 * <em>option</em> appears in a command line.
 * @see OptionArgument
 * @see Argument
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Option {

    public static final char NO_SHORT = '\0';
    
    /** 
     * The name of the option (without the initial {@code --}). 
     * Defaults to a name based on the setter name
     */
    String longName() default "";
    
    /**
     * The short option (without the initial {@code -}). 
     * If not given the option has no short name.
     */
    char shortName() default NO_SHORT;
    
}
