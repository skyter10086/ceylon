/********************************************************************************
 * Copyright (c) 2011-2017 Red Hat Inc. and/or its affiliates and others
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Apache License, Version 2.0 which is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: Apache-2.0 
 ********************************************************************************/
package org.eclipse.ceylon.compiler.typechecker.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.ceylon.compiler.typechecker.io.VirtualFile;
import org.eclipse.ceylon.model.typechecker.model.Unit;

public abstract class PhasedUnitMap<ReturnedType extends PhasedUnit, StoredType> {

    protected Map<String, StoredType> phasedUnitPerPath = new LinkedHashMap<String, StoredType>();
    protected Map<String, String> relativePathToPath = new HashMap<String, String>();

    protected abstract StoredType toStoredType(ReturnedType phasedUnit); 
    protected abstract ReturnedType fromStoredType(StoredType storedValue, String path); 
    
    public boolean containsRelativePath(String relativePath) {
        return relativePathToPath.containsKey(relativePath);
    }
    
    public void addPhasedUnit(VirtualFile unitFile, ReturnedType phasedUnit) {
        phasedUnitPerPath.put(unitFile.getPath(), toStoredType(phasedUnit));
        relativePathToPath.put(phasedUnit.getPathRelativeToSrcDir(), unitFile.getPath());
    }

    public void removePhasedUnitForRelativePath(String relativePath) {
        String path = relativePathToPath.get(relativePath);
        ReturnedType phasedUnit = fromStoredType(phasedUnitPerPath.get(path), path);
        if (phasedUnit != null) {
            Unit unit = phasedUnit.getUnit();
            if (unit != null) {
                unit.getPackage().removeUnit(unit);
            }
        }
        relativePathToPath.remove(relativePath);
        phasedUnitPerPath.remove(path);
    }

    protected void addInReturnedList(List<ReturnedType> list, ReturnedType phasedUnit) {
        list.add(phasedUnit);
    }
    
    public List<ReturnedType> getPhasedUnits() {
        List<ReturnedType> list = new ArrayList<ReturnedType>(phasedUnitPerPath.size());
        for (Entry<String, StoredType> entry : phasedUnitPerPath.entrySet()) {
            addInReturnedList(list, fromStoredType(entry.getValue(), entry.getKey()));
        }
        return list;
    }

    public ReturnedType getPhasedUnit(VirtualFile file) {
        return getPhasedUnit(file.getPath());
    }

    public ReturnedType getPhasedUnit(String path) {
        StoredType storedValue = phasedUnitPerPath.get(path);
        return storedValue == null ? null : fromStoredType(storedValue, path); 
    }

    public ReturnedType getPhasedUnitFromRelativePath(String relativePath) {
        if (relativePath.startsWith("/")) {
            relativePath = relativePath.substring(1);
        }
        return getPhasedUnit(relativePathToPath.get(relativePath));
    }
}