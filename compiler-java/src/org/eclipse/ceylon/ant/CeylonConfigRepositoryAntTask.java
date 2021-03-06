/*
 * Copyright Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the authors tag. All rights reserved.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License version 2.
 * 
 * This particular file is subject to the "Classpath" exception as provided in the 
 * LICENSE file that accompanied this code.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.eclipse.ceylon.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.eclipse.ceylon.common.config.CeylonConfig;
import org.eclipse.ceylon.common.config.Credentials;
import org.eclipse.ceylon.common.config.Repositories;
import org.eclipse.ceylon.common.config.Repositories.Repository;

public class CeylonConfigRepositoryAntTask extends CeylonConfigBaseTask {
    
    public enum Type { system, output, cache, lookup, global, remote, other }
    public enum Key { name, url, user, password, keystore, alias }
    
    private String name;
    private Type type;
    private Key key;
    private String property;
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    @Override
    public void execute() throws BuildException {
        Java7Checker.check();
        if (name == null && type == null) {
            throw new BuildException("Either the 'name' or 'type' attribute has to be specified for 'ceylon-config-repository'");
        }
        if (name != null && type != null) {
            throw new BuildException("The 'name' and 'type' attributes cannot both be specified for 'ceylon-config-repository'");
        }
        if (key == null) {
            throw new BuildException("'key' is a required attribute for 'ceylon-config-repository'");
        }
        if (property == null) {
            throw new BuildException("'property' is a required attribute for 'ceylon-config-value'");
        }
        CeylonConfig config = getConfig();
        Repositories reps = Repositories.withConfig(config);
        Repository rep;
        String repdesc;
        if (name != null) {
            rep = reps.getRepository(name);
            repdesc = "of name '" + name + "'";
        } else {
            rep = reps.getRepositoryByTypeWithDefaults(type.name());
            repdesc = "of type '" + type.name() + "'";
        }
        if (rep != null) {
            String value = null;
            switch (key) {
            case name:
                value = rep.getName(); break;
            case url:
                value = rep.getUrl(); break;
            default:
                Credentials cred = rep.getCredentials();
                if (cred != null) {
                    switch (key) {
                    case user:
                        value = cred.getUser(); break;
                    case password:
                        value = cred.getPassword(); break;
                    case keystore:
                        value = cred.getKeystore(); break;
                    case alias:
                        value = cred.getAlias(); break;
                    default:
                        // Won't happen
                    }
                } else {
                    log("No credentials found for repository " + repdesc, Project.MSG_WARN);
                }
            }
            if (value != null) {
                setProjectProperty(property, value);
            }
        } else {
            log("Repository " + repdesc + " not found", Project.MSG_WARN);
        }
    }

}
