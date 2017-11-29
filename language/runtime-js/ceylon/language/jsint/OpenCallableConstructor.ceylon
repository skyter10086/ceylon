/********************************************************************************
 * Copyright (c) 2011-2017 Red Hat Inc. and/or its affiliates and others
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Apache License, Version 2.0 which is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: Apache-2.0 
 ********************************************************************************/
import ceylon.language.meta.declaration {
  CallableConstructorDeclaration, FunctionOrValueDeclaration, Package,
  ClassDeclaration, Module, OpenType, TypeParameter
}
import ceylon.language.meta.model {
  CallableConstructor, MemberClassCallableConstructor, Type,
  FunctionModel, Applicable
}
import ceylon.language { AnnotationType=Annotation }

shared native class OpenCallableConstructor(containingPackage, shared Anything meta) satisfies CallableConstructorDeclaration {
  shared actual native Boolean abstract;
  shared actual native Boolean defaultConstructor;
  shared actual native ClassDeclaration container;
  shared actual native Object invoke(Type<>[] typeArguments, Anything* arguments);
  shared actual native Object memberInvoke(Object container, Type<>[] typeArguments, Anything* arguments);
  shared actual native CallableConstructor<Result,Arguments> apply<Result=Object,Arguments=Nothing>(Type<>* typeArguments)
            given Arguments satisfies Anything[];
  shared actual native MemberClassCallableConstructor<Container,Result,Arguments> memberApply<Container=Nothing,Result=Object,Arguments=Nothing>(Type<Object> containerType, Type<>* typeArguments)
            given Arguments satisfies Anything[];
  shared actual native FunctionModel<Return, Arguments>& Applicable<Return, Arguments>
         staticApply<Return=Anything, Arguments=Nothing>(
     Type<Object> containerType, Type<>* typeArguments)
         given Arguments satisfies Anything[];

  shared actual native Boolean shared;
  shared actual native Boolean formal;
  shared actual native Boolean default;
  shared actual native Boolean actual;
  shared actual native Boolean static;
  //FunctionalDeclaration
  annotation=false;
  shared actual native FunctionOrValueDeclaration[] parameterDeclarations;
  shared actual native FunctionOrValueDeclaration? getParameterDeclaration(String name);
  //Contained
  toplevel=false;
  shared actual Package containingPackage;
  shared actual native Module containingModule => containingPackage.container;
  //TypedDeclaration
  shared actual native OpenType openType;
  //AnnotatedDeclaration
  shared actual native Annotation[] annotations<out Annotation>()
        given Annotation satisfies AnnotationType;
  shared actual native Boolean annotated<Annotation>()
          given Annotation satisfies AnnotationType;
  //Declaration
  shared actual native String name;
  shared actual native String qualifiedName;
  shared actual String string => "new ``qualifiedName``";
  shared actual native Boolean equals(Object other);

  //GenericDeclaration
  shared actual native TypeParameter[] typeParameterDeclarations;
  shared actual native TypeParameter? getTypeParameterDeclaration(String name);
}
