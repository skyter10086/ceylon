/********************************************************************************
 * Copyright (c) 2011-2017 Red Hat Inc. and/or its affiliates and others
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Apache License, Version 2.0 which is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: Apache-2.0 
 ********************************************************************************/
package org.eclipse.ceylon.compiler.typechecker.util;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.ceylon.compiler.typechecker.analyzer.UsageWarning;
import org.eclipse.ceylon.compiler.typechecker.analyzer.Warning;
import org.eclipse.ceylon.compiler.typechecker.tree.Message;
import org.eclipse.ceylon.compiler.typechecker.tree.Node;
import org.eclipse.ceylon.compiler.typechecker.tree.Tree;
import org.eclipse.ceylon.compiler.typechecker.tree.TreeUtil;
import org.eclipse.ceylon.compiler.typechecker.tree.Visitor;
import org.eclipse.ceylon.compiler.typechecker.tree.Tree.Annotation;
import org.eclipse.ceylon.compiler.typechecker.tree.Tree.AnnotationList;
import org.eclipse.ceylon.compiler.typechecker.tree.Tree.Primary;
import org.eclipse.ceylon.model.typechecker.model.Declaration;

public class WarningSuppressionVisitor<E extends Enum<E>> 
        extends Visitor {

    private Class<E> enumType;
    
    /** 
     * The current set of suppressed warnings, and whether they're 
     * suppressed by an annotation (or by an option) 
     */
    private EnumMap<E,Boolean> suppressed;
    /** 
     * On-going count of how many times each warning has been suppressed 
     */
    private EnumMap<E, Integer> counts;
    
    public WarningSuppressionVisitor(Class<E> enumType, EnumSet<E> initial) {
        this.enumType = enumType;
        this.suppressed = new EnumMap<E, Boolean>(enumType);
        for (E e : initial) {
            this.suppressed.put(e, false);
        }
        this.counts = new EnumMap<E, Integer>(enumType);
        for (E e : EnumSet.allOf(enumType)) {
            this.counts.put(e, 0);
        }
    }
    
    private E parseName(String name) {
        try {
            return Enum.valueOf(enumType, name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    public void visitAny(Node that) {
        Iterator<Message> errorIter = that.getErrors().iterator();
        while (errorIter.hasNext()) {
            Message error = errorIter.next();
            if (error instanceof UsageWarning) {
                UsageWarning warning = (UsageWarning)error;
                E warningName = parseName(warning.getWarningName());
                if (warningName == null) {
                    continue;
                }
                Integer numSuppressed = counts.get(warningName);
                if (suppressed.get(warningName) != null) {
                    warning.setSuppressed(true);
                    counts.put(warningName, numSuppressed.intValue()+1);
                }
            }
        }
        super.visitAny(that);
    }
    
    public void visit(Tree.Declaration that) {
        if (!TreeUtil.isForUnsupportedBackend(that.getAnnotationList(), that.getUnit())) {
            EnumMap<E, Integer> priorCounts = counts.clone();
            EnumMap<E,Boolean> added = pre(that.getAnnotationList());
            super.visit(that);
            post(priorCounts, added, that.getAnnotationList());
        }
    }
    
    public void visit(Tree.ModuleDescriptor that) {
        if (!TreeUtil.isForUnsupportedBackend(that.getAnnotationList(), that.getUnit())) {
            EnumMap<E, Integer> priorCounts = counts.clone();
            EnumMap<E,Boolean> added = pre(that.getAnnotationList());
            super.visit(that);
            post(priorCounts, added, that.getAnnotationList());
        }
    }
    
    public void visit(Tree.PackageDescriptor that) {
        EnumMap<E, Integer> priorCounts = counts.clone();
        EnumMap<E,Boolean> added = pre(that.getAnnotationList());
        super.visit(that);
        post(priorCounts, added, that.getAnnotationList());
    }
    
    public void visit(Tree.ImportModule that) {
        if (!TreeUtil.isForUnsupportedBackend(that.getAnnotationList(), that.getUnit())) {
            EnumMap<E, Integer> priorCounts = counts.clone();
            EnumMap<E,Boolean> added = pre(that.getAnnotationList());
            super.visit(that);
            post(priorCounts, added, that.getAnnotationList());
        }
    }
    
    public void visit(Tree.Assertion that) {
        EnumMap<E, Integer> priorCounts = counts.clone();
        EnumMap<E,Boolean> added = pre(that.getAnnotationList());
        super.visit(that);
        post(priorCounts, added, that.getAnnotationList());
    }

    private EnumMap<E,Boolean> pre(AnnotationList al) {
        EnumMap<E,Boolean> added = new EnumMap<E,Boolean>(enumType);
        for (Map.Entry<E, Tree.StringLiteral> entry : getWarningNames(findSuppressWarnings(al), true).entrySet()) {
            E warning = entry.getKey();
            Boolean suppressedByAnnotation = this.suppressed.get(warning);
            if (suppressedByAnnotation == null) {// not suppressed
                this.suppressed.put(warning, true);
                added.put(warning, null);
            } else if (suppressedByAnnotation) {// already suppressed by annotation
                entry.getValue().addUsageWarning(Warning.suppressedAlready, "warnings already suppressed by annotation");
                added.put(warning, true);
            } else {// already suppressed by option
                this.suppressed.put(warning, true);
                added.put(warning, false);
            }
        }
        return added;
    }

    private void post(EnumMap<E, Integer> priorCounts, EnumMap<E,Boolean> added, AnnotationList al) {
        for (Map.Entry<E, Tree.StringLiteral> warningName : getWarningNames(findSuppressWarnings(al), false).entrySet()) {
            Integer before = priorCounts.get(warningName.getKey());
            if (before == null) {
                before = 0;
            }
            Integer after = counts.get(warningName.getKey());
            if (after == null) {
                after = 0;
            }
            if (after - before == 0) {
                if (suppressed.get(Warning.suppressesNothing) == null) {
                    warningName.getValue().addUsageWarning(Warning.suppressesNothing, "suppresses no warnings");
                }
            }
        }
        this.suppressed.putAll(added);
    }
    
    public Tree.Annotation findSuppressWarnings(Tree.AnnotationList that) {
        if (that != null && that.getAnnotations() != null) {
            for (Tree.Annotation anno : that.getAnnotations()) {
                Annotation suppressWarnings = getSuppressWarnings(anno);
                if (suppressWarnings != null) {
                    return suppressWarnings;
                }
            }
        }
        return null;
    }
    
    private static Tree.Annotation getSuppressWarnings(
            Tree.Annotation anno) {
        Primary primary = anno.getPrimary();
        if (primary instanceof Tree.MemberOrTypeExpression) {
            Tree.MemberOrTypeExpression mte = 
                    (Tree.MemberOrTypeExpression)primary;
            Declaration declaration = mte.getDeclaration();
            if (declaration != null 
                    && declaration.equals(anno.getUnit()
                            .getLanguageModuleDeclaration(
                                    "suppressWarnings"))) {
                return anno;
            }
        }
        return null;
    }
    
    private Map<E, Tree.StringLiteral> getWarningNames(Tree.Annotation anno, 
            final boolean warnAboutUnknownWarnings) {
        if (anno == null) {
            return Collections.emptyMap();
        }
        final Map<E, Tree.StringLiteral> suppressed = new HashMap<E, Tree.StringLiteral>(2);
        anno.visit(new Visitor() {
            public void visit(Tree.StringLiteral that) {
                String warningName = that.getText();
                E warning = parseName(warningName);
                if (warning == null) {
                    if (warnAboutUnknownWarnings && 
                            suppressed.get(Warning.unknownWarning) == null) {
                        that.addUsageWarning(Warning.unknownWarning, "unknown warning: " + warningName);
                    }
                } else {
                    suppressed.put(warning, that);
                }
            }
        });
        return suppressed;
    }
}
