/********************************************************************************
 * Copyright (c) 2011-2017 Red Hat Inc. and/or its affiliates and others
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Apache License, Version 2.0 which is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: Apache-2.0 
 ********************************************************************************/
package org.eclipse.ceylon.model.loader.model;

import java.util.List;

import org.eclipse.ceylon.model.loader.JvmBackendUtil;
import org.eclipse.ceylon.model.loader.ModelCompleter;
import org.eclipse.ceylon.model.loader.mirror.ClassMirror;
import org.eclipse.ceylon.model.typechecker.model.Annotation;
import org.eclipse.ceylon.model.typechecker.model.ClassAlias;
import org.eclipse.ceylon.model.typechecker.model.Declaration;
import org.eclipse.ceylon.model.typechecker.model.Parameter;
import org.eclipse.ceylon.model.typechecker.model.ParameterList;
import org.eclipse.ceylon.model.typechecker.model.Reference;
import org.eclipse.ceylon.model.typechecker.model.Scope;
import org.eclipse.ceylon.model.typechecker.model.Type;
import org.eclipse.ceylon.model.typechecker.model.TypeParameter;
import org.eclipse.ceylon.model.typechecker.model.Unit;

/**
 * Represents a lazy class alias.
 *
 * @author Stéphane Épardaud <stef@epardaud.fr>
 */
public class LazyClassAlias extends ClassAlias implements LazyContainer {

    public ClassMirror classMirror;
    private ModelCompleter completer;
    
    private boolean isLoaded = false;
    private boolean isLoaded2 = false;
    private boolean isTypeParamsLoaded = false;
    private boolean isTypeParamsLoaded2 = false;
    private boolean local;

    @Override
    protected java.lang.Class<?> getModelClass() {
        return getClass().getSuperclass(); 
    }
    
    public LazyClassAlias(ClassMirror classMirror, ModelCompleter completer) {
        this.classMirror = classMirror;
        this.completer = completer;
        setName(JvmBackendUtil.getMirrorName(classMirror));
    }

    private void load() {
        if(!isLoaded2){
            completer.synchronizedRun(new Runnable() {
                @Override
                public void run() {
                    loadTypeParams();
                    if(!isLoaded){
                        isLoaded = true;
                        completer.complete(LazyClassAlias.this);
                        isLoaded2 = true;
                    }
                }
            });
        }
    }

    private void loadTypeParams() {
        if(!isTypeParamsLoaded2){
            completer.synchronizedRun(new Runnable() {
                @Override
                public void run() {
                    if(!isTypeParamsLoaded){
                        isTypeParamsLoaded = true;
                        completer.completeTypeParameters(LazyClassAlias.this);
                        isTypeParamsLoaded2 = true;
                    }
                }
            });
        }
    }
    
    @Override
    public String toString() {
        if (!isLoaded) {
            return "UNLOADED:" + super.toString();
        }
        return super.toString();
    }

    @Override
    public org.eclipse.ceylon.model.typechecker.model.TypeDeclaration getConstructor() {
        load();
        return super.getConstructor();
    };
    
    @Override
    public ParameterList getParameterList() {
        load();
        return super.getParameterList();
    }

    @Override
    public boolean isStatic() {
        // no lazy loading since it is set before completion
        return super.isStatic();
    }

    @Override
    public Parameter getParameter(String name) {
        load();
        return super.getParameter(name);
    }

    @Override
    public List<Declaration> getMembers() {
        load();
        return super.getMembers();
    }

    @Override
    public Type getType() {
        loadTypeParams();
        return super.getType();
    }

    @Override
    public Type getExtendedType() {
        load();
        return super.getExtendedType();
    }
    
    @Override
    public List<TypeParameter> getTypeParameters() {
        loadTypeParams();
        return super.getTypeParameters();
    }
    
    @Override
    public List<ParameterList> getParameterLists() {
        load();
        return super.getParameterLists();
    }

    @Override
    public boolean isMember() {
        // NO lazy-loading since this uses getContainer() which is set before lazy-loading
        return super.isMember();
    }

    @Override
    public Type getDeclaringType(Declaration d) {
        load();
        return super.getDeclaringType(d);
    }

    @Override
    public boolean isParameterized() {
        loadTypeParams();
        return super.isParameterized();
    }

    @Override
    public List<Type> getSatisfiedTypes() {
        load();
        return super.getSatisfiedTypes();
    }

    @Override
    public List<Type> getCaseTypes() {
        load();
        return super.getCaseTypes();
    }

    @Override
    public Reference appliedReference(Type pt,
            List<Type> typeArguments) {
        loadTypeParams();
        return super.appliedReference(pt, typeArguments);
    }

    @Override
    public Type appliedType(Type outerType,
            List<Type> typeArguments) {
        loadTypeParams();
        return super.appliedType(outerType, typeArguments);
    }

    @SuppressWarnings("deprecation")
    @Override
    public List<Declaration> getInheritedMembers(String name) {
        load();
        return super.getInheritedMembers(name);
    }

    @Override
    public Declaration getRefinedMember(String name, List<Type> signature, boolean ellipsis) {
        load();
        return super.getRefinedMember(name, signature, ellipsis);
    }
    
    @Override
    public Declaration getMember(String name, List<Type> signature, boolean ellipsis) {
        load();
        return super.getMember(name, signature, ellipsis);
    }

    @Override
    public Declaration getMemberOrParameter(String name, List<Type> signature, boolean ellipsis) {
        load();
        return super.getMemberOrParameter(name, signature, ellipsis);
    }

    @Override
    public boolean isAlias() {
        // does not require lazy loading since it depends on class
        return super.isAlias();
    }

    @Override
    public Type getSelfType() {
        load();
        return super.getSelfType();
    }

    @Override
    public Scope getVisibleScope() {
        // NO lazy-loading since this uses getContainer() which is set before lazy-loading
        return super.getVisibleScope();
    }

    @Override
    public List<Annotation> getAnnotations() {
        load();
        return super.getAnnotations();
    }

    @Override
    public String getQualifiedNameString() {
        load();
        return super.getQualifiedNameString();
    }

    @Override
    public boolean isActual() {
        load();
        return super.isActual();
    }

    @Override
    public boolean isFormal() {
        load();
        return super.isFormal();
    }

    @Override
    public boolean isDefault() {
        load();
        return super.isDefault();
    }

    @Override
    public boolean isVisible(Scope scope) {
        // NO lazy-loading since this uses getContainer() which is set before lazy-loading
        return super.isVisible(scope);
    }

    @Override
    public boolean isDefinedInScope(Scope scope) {
        load();
        return super.isDefinedInScope(scope);
    }

    @Override
    public boolean isCaptured() {
        load();
        return super.isCaptured();
    }

    @Override
    public boolean isToplevel() {
        // NO lazy-loading since this uses getContainer() which is set before lazy-loading
        return super.isToplevel();
    }

    @Override
    public boolean isClassMember() {
        // NO lazy-loading since this uses getContainer() which is set before lazy-loading
        return super.isClassMember();
    }

    @Override
    public boolean isInterfaceMember() {
        // NO lazy-loading since this uses getContainer() which is set before lazy-loading
        return super.isInterfaceMember();
    }

    @Override
    public boolean isClassOrInterfaceMember() {
        // NO lazy-loading since this uses getContainer() which is set before lazy-loading
        return super.isClassOrInterfaceMember();
    }

    @Override
    public Unit getUnit() {
        // this doesn't require to load the model
        return super.getUnit();
    }

    @Override
    public Scope getContainer() {
        // NO lazy-loading since this is set before lazy-loading
        return super.getContainer();
    }

    @Override
    public Declaration getDirectMember(String name, List<Type> signature, boolean ellipsis) {
        load();
        return super.getDirectMember(name, signature, ellipsis);
    }

    @Override
    public Declaration getMemberOrParameter(Unit unit, String name, List<Type> signature, boolean ellipsis) {
        load();
        return super.getMemberOrParameter(unit, name, signature, ellipsis);
    }

    @Override
    public void addMember(Declaration decl) {
        // do this without lazy-loading
        super.addMember(decl);
    }

    @Override
    public boolean isLoaded() {
        return isLoaded;
    }

    @Override
    public void setLocal(boolean local) {
        this.local = local;
    }
    
    @Override
    public boolean isLocal(){
        return this.local ;
    }

    @Override
    public Declaration getLocalDeclaration(String name) {
        return null;
    }

    @Override
    public void addLocalDeclaration(Declaration declaration) {
        throw new RuntimeException("type aliases do not contain any local declarations");
    }

    @Override
    public boolean isDeprecated() {
        // requires no lazy-loading
        return super.isDeprecated();
    }

    @Override
    public boolean isCeylon() {
        // there are no aliases in Java
        return true;
    }
}
