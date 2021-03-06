/********************************************************************************
 * Copyright (c) 2011-2017 Red Hat Inc. and/or its affiliates and others
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Apache License, Version 2.0 which is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: Apache-2.0 
 ********************************************************************************/
package org.eclipse.ceylon.cmr.impl;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.eclipse.ceylon.cmr.api.ArtifactContext;
import org.eclipse.ceylon.cmr.api.CmrRepository;
import org.eclipse.ceylon.cmr.api.ModuleDependencyInfo;
import org.eclipse.ceylon.cmr.api.ModuleInfo;
import org.eclipse.ceylon.cmr.api.Overrides;
import org.eclipse.ceylon.cmr.api.RepositoryManager;
import org.eclipse.ceylon.cmr.spi.Node;
import org.eclipse.ceylon.cmr.spi.OpenNode;
import org.eclipse.ceylon.common.ModuleUtil;
import org.eclipse.ceylon.model.cmr.ArtifactResult;

/**
 * Repository which looks modules up from a flat repository.
 *
 * @author Stephane Epardaud
 */
public class FlatRepository extends DefaultRepository {

    public FlatRepository(OpenNode root) {
        super(root);
    }

    @Override
    protected List<String> getDefaultParentPathInternal(ArtifactContext context) {
        // search at the root
        return Collections.emptyList();
    }
    
    @Override
    public String[] getArtifactNames(ArtifactContext context) {
        String replacedName = context.getName().replace(':', '.');
        return getArtifactNames(replacedName, context.getVersion(), context.getSuffixes());
    }
    
    @Override
    protected ArtifactResult getArtifactResultInternal(RepositoryManager manager, Node node) {
        return new FlatArtifactResult(this, manager, node);
    }
    
    @Override
    public boolean supportsNamespace(String searchedNamespace) {
        return true;
    }
    
    protected static class FlatArtifactResult extends DefaultArtifactResult {

        public FlatArtifactResult(CmrRepository repository, RepositoryManager manager, Node node) {
            super(repository, manager, node);
        }
        
        @Override
        protected ModuleInfo resolve() {
            ModuleInfo dependencies = super.resolve();
            if(dependencies == null){
                // try to resolve them from other flat repos
                for(CmrRepository repo : getManager().getRepositories()){
                    if(repo instanceof FlatRepository){
                        dependencies = getExternalDescriptor(repo, XmlDependencyResolver.INSTANCE);
                        if(dependencies == null)
                            dependencies = getExternalDescriptor(repo, PropertiesDependencyResolver.INSTANCE);
                        // stop looking if we have found it
                        if(dependencies != null)
                            break;
                    }
                }
                if (dependencies == null) {
                    Overrides overrides = ((CmrRepository)repository()).getRoot().getService(Overrides.class);
                    if(overrides != null) {
                        // don't use groupId() because it calls resolve() and we have no external info
                        String[] coordinates = ModuleUtil.getMavenCoordinates(name());
                        dependencies = new ModuleInfo(namespace(), name(), version(), coordinates[0], coordinates[1], 
                                coordinates[2], null, new HashSet<ModuleDependencyInfo>());
                        dependencies = overrides.applyOverrides(name(), version(), dependencies);
                    }
                    
                }
            }
            return dependencies;
        }

        private ModuleInfo getExternalDescriptor(CmrRepository repo, ModulesDependencyResolver resolver) {
            String moduleXml = resolver.getQualifiedToplevelDescriptorName(name(), version());
            Overrides overrides = repo.getRoot().getService(Overrides.class);
            Node moduleXmlNode = repo.getRoot().getChild(moduleXml);
            if(moduleXmlNode != null){
                File f = null;
                try {
                    f = moduleXmlNode.getContent(File.class);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if(f != null)
                    return resolver.resolveFromFile(f, name(), version(), overrides);
            }
            return null;
        }
    }
}
