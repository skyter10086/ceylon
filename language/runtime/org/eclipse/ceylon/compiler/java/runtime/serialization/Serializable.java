/********************************************************************************
 * Copyright (c) 2011-2017 Red Hat Inc. and/or its affiliates and others
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Apache License, Version 2.0 which is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: Apache-2.0 
 ********************************************************************************/
package org.eclipse.ceylon.compiler.java.runtime.serialization;

import java.util.Collection;

import org.eclipse.ceylon.compiler.java.metadata.Ignore;

import ceylon.language.serialization.ReachableReference;

/**
 * Interface implicitly implemented by {@code serializable} classes. 
 * The methods in this interface are generated 
 * automatically by the compiler.
 * 
 * @see $Serialization$
 */
@Ignore
public interface Serializable {
    
    /**
     * Get the value of one of the references returned from {@link #$references$()}
     * @param attributeOrIndexOrOuter
     * @return
     */
    public java.lang.Object $get$(ReachableReference ref);
    /** 
     * Return a collection of objects identifying the assignable state of 
     * the class of this instance.
     * 
     * Each Ceylon attributes qualified name will be included {@code java.lang.String}. 
     * For arrays the index of each element will be included {@code java.lang.Integer}.
     * 
     * This is static data, but since the caller doesn't know the type 
     * as compile time, it is easiest to make this a virtual method.
     * */
    public Collection<ReachableReference> $references$();
    /**
     * Set the given attribute or element to the given value.
     * @param attributeOrIndex
     * @param ref
     */
    public void $set$(ReachableReference reference, java.lang.Object instance);
}
