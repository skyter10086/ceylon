/********************************************************************************
 * Copyright (c) 2011-2017 Red Hat Inc. and/or its affiliates and others
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Apache License, Version 2.0 which is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: Apache-2.0 
 ********************************************************************************/
package org.eclipse.ceylon.model.typechecker.model;

import java.util.List;

public interface ImportScope {
    public List<Import> getImports();
    public void addImport(Import imp);
    public void removeImport(Import imp);
    public Import getImport(String name);
    public List<Declaration> getMembers();
}
