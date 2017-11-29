/********************************************************************************
 * Copyright (c) 2011-2017 Red Hat Inc. and/or its affiliates and others
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Apache License, Version 2.0 which is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: Apache-2.0 
 ********************************************************************************/
package org.eclipse.ceylon.common.tools.config;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;

import org.eclipse.ceylon.common.OSUtil;
import org.eclipse.ceylon.common.config.CeylonConfig;
import org.eclipse.ceylon.common.config.CeylonConfigFinder;
import org.eclipse.ceylon.common.config.ConfigFinder;
import org.eclipse.ceylon.common.config.ConfigWriter;
import org.eclipse.ceylon.common.tool.Argument;
import org.eclipse.ceylon.common.tool.CeylonBaseTool;
import org.eclipse.ceylon.common.tool.Description;
import org.eclipse.ceylon.common.tool.Option;
import org.eclipse.ceylon.common.tool.OptionArgument;
import org.eclipse.ceylon.common.tool.RemainingSections;
import org.eclipse.ceylon.common.tool.Subtool;
import org.eclipse.ceylon.common.tool.Summary;
import org.eclipse.ceylon.common.tool.Tool;
import org.eclipse.ceylon.common.tools.CeylonTool;

@Summary("Manages Ceylon configuration files")
@Description(
"Can be used to list, update and remove settings in Ceylon's configuration files.\n" +
"\n" +
"Setting names are of form `<section>.<key>`, for example, `defaults.encoding`.\n"
)
@RemainingSections(
"##EXAMPLE\n" +
"\n" +
"The following would list the settings active from within the current folder:\n" +
"\n" +
"    ceylon config list\n" +
"\n" +
"This reads a named setting:\n" +
"\n" +
"    ceylon config get defaults.encoding\n" +
"\n" +
"This writes a named setting:\n" +
"\n" +
"    ceylon config --file=.ceylon/config set defaults.encoding UTF-8\n" +
"\n"
)
public class CeylonConfigTool extends CeylonBaseTool {
    
    private Tool action;
    
    private File file;
    private boolean system;
    private boolean user;
    private boolean local;
    
    private int configFileArgCount = 0;
    
    @OptionArgument(argumentName="file")
    @Description("The file to operate on.")
    public void setFile(File file) {
        this.file = file;
    }
    
    @Option(longName="system")
    @Description("Apply operation to the system configuration.")
    public void setSystem(boolean system) {
        this.system = system;
    }
    
    @Option(longName="user")
    @Description("Apply operation to the user configuration.")
    public void setUser(boolean user) {
        this.user = user;
    }
    
    @Option(longName="local")
    @Description("Apply operation to the local configuration.")
    public void setLocal(boolean local) {
        this.local = local;
    }
    
    @Subtool(argumentName="action",
            classes={List.class, Get.class, Set.class, Remove.class, RenameSection.class, RemoveSection.class, Keystore.class})
    public void setAction(Tool action) {
        this.action = action;
    }

    private CeylonConfig readConfig() throws IOException {
        ConfigFinder finder = CeylonConfigFinder.DEFAULT;
        if (file != null) {
            if (file.exists()) {
                return finder.loadConfigFromFile(applyCwd(file));
            } else {
                return new CeylonConfig();
            }
        } else {
            try {
                if (system) {
                    return finder.loadSystemConfig();
                }
                if (user) {
                    return finder.loadUserConfig();
                }
                if (local) {
                    File config = finder.findLocalConfig(applyCwd(new File(".")));
                    if(config == null)
                        return new CeylonConfig();
                    return finder.loadConfigFromFile(config);
                }
            } catch (FileNotFoundException ex) {
                return new CeylonConfig();
            }
            return CeylonConfig.get();
        }
    }
    
    private void writeConfig(CeylonConfig config) throws IOException {
        ConfigFinder finder = CeylonConfigFinder.DEFAULT;
        File cfgFile;
        if (file != null) {
            cfgFile = applyCwd(file);
        } else {
            if (system) {
                cfgFile = finder.findSystemConfig();
            } else if (user) {
                cfgFile = finder.findUserConfig();
            } else if (local) {
                cfgFile = finder.findLocalConfig(applyCwd(new File(".")));
            } else {
                throw new IllegalStateException("A configuration must be specified");
            }
        }
        ConfigWriter.instance().write(config, cfgFile);
    }
    
    private void initSubtool() {
        configFileArgCount = 0;
        if (file != null) configFileArgCount++;
        if (system) configFileArgCount++;
        if (user) configFileArgCount++;
        if (local) configFileArgCount++;
        
        if (configFileArgCount > 1) {
            throw new IllegalStateException("Only one argument specifying a configuration can be used at a time");
        }
    }
    
    private void initUpdatingSubtool() {
        initSubtool();
        if (configFileArgCount == 0) {
            throw new IllegalStateException("A configuration must be specified");
        }
    }
    
    @Description("Lists configuration values")
    public class List implements Tool {

        @Override
        public void initialize(CeylonTool mainTool) {
            initSubtool();
        }

        @Override
        public void run() throws IOException {
            CeylonConfig config = readConfig();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            (new RichConfigWriter()).write(config, out);
            System.out.print(out.toString("UTF-8"));
        }
    }

    @Description("Get the value defined for `<key>` in the config file")
    public class Get implements Tool {
    
        private String key;
        
        @Argument(argumentName="key", multiplicity="1")
        public void setKey(String key) {
            this.key = key;
        }

        @Override
        public void initialize(CeylonTool mainTool) {
            initSubtool();
        }
        
        @Override
        public void run() throws IOException {
            CeylonConfig config = readConfig();
            String[] values = config.getOptionValues(key);
            if (values != null) {
                for (String value : values) {
                    System.out.println(ConfigWriter.escape(value));
                }
            }
        }
    }
    
    @Description("Set the value of the `<key>` to `<values>` in the config file")
    public class Set implements Tool {

        private String key;
        private java.util.List<String> values;
        
        @Argument(argumentName="key", multiplicity="1", order=1)
        public void setKey(String key) {
            this.key = key;
        }
        
        @Argument(argumentName="values", multiplicity="+", order=2)
        public void setValues(java.util.List<String> values) {
            this.values = values;
        }

        @Override
        public void initialize(CeylonTool mainTool) {
            initUpdatingSubtool();
        }
        
        @Override
        public void run() throws IOException {
            CeylonConfig config = readConfig();
            String[] vals = values.toArray(new String[values.size()]);
            try {
                config.setOptionValues(key, vals);
            } catch (IllegalArgumentException ex) {
                throw new ConfigException(ex.getMessage());
            }
            writeConfig(config);
        }
    }
    
    @Description("Removes the value of the `<key>` in the config file")
    public class Remove implements Tool {
        
        private String key;
        
        @Argument(argumentName="key", multiplicity="1")
        public void setKey(String key) {
            this.key = key;
        }

        @Override
        public void initialize(CeylonTool mainTool) {
            initUpdatingSubtool();
        }
        
        @Override
        public void run() throws IOException {
            CeylonConfig config = readConfig();
            try {
                config.removeOption(key);
            } catch (IllegalArgumentException ex) {
                throw new ConfigException(ex.getMessage());
            }
            writeConfig(config);
        }
    }
    
    @Description("Renames the section `<old-name>` in the config file to `<new-name>`")
    public class RenameSection implements Tool {
        
        private String oldName;
        private String newName;
        
        @Argument(argumentName="old-name", multiplicity="1", order=1)
        public void setOldName(String oldName) {
            this.oldName = oldName;
        }
        
        @Argument(argumentName="new-name", multiplicity="1", order=2)
        public void setNewName(String newName) {
            this.newName = newName;
        }

        @Override
        public void initialize(CeylonTool mainTool) {
            initUpdatingSubtool();
        }
        
        @Override
        public void run() throws IOException {
            throw new ConfigException("Not implemented yet");
        }
    }
    
    @Description("Removes the named `<section>` from the config file")
    public class RemoveSection implements Tool {
        
        private String name;
        
        @Argument(argumentName="name", multiplicity="1", order=1)
        public void setSection(String name) {
            this.name = name;
        }

        @Override
        public void initialize(CeylonTool mainTool) {
            initUpdatingSubtool();
        }
        
        @Override
        public void run() throws IOException {
            CeylonConfig config = readConfig();
            try {
                config.removeSection(name);
            } catch (IllegalArgumentException ex) {
                throw new ConfigException(ex.getMessage());
            }
            writeConfig(config);
        }
    }
    
    @Description("Modifies keystores")
    public class Keystore implements Tool {
        private Tool tool;

        private String storePassword;
        
        @OptionArgument
        @Option
        @Description("The password for accessing the keystore")
        public void setStorePassword(String storePassword) {
            this.storePassword = storePassword;
        }
        
        @Description("Gets the password for `<alias>` in the keystore")
        public class GetPassword implements Tool {
            
            private String alias;
            
            @Argument(argumentName="alias", multiplicity="1", order=1)
            public void setAlias(String alias) {
                this.alias = alias;
            }
            
            @Override
            public void initialize(CeylonTool mainTool) {
            }
            
            @Override
            public void run() throws Exception {
                // TODO Auto-generated method stub
                
            }
        }
        
        @Description("Sets the password for `<alias>` in the keystore. " +
        		"The program will issue a password prompt if `<password>` is omitted.")
        public class SetPassword implements Tool {
            
            private String alias;
            
            private String password;
            
            @Argument(argumentName="alias", multiplicity="1", order=1)
            public void setAlias(String alias) {
                this.alias = alias;
            }
            
            @Argument(argumentName="password", multiplicity="?", order=2)
            public void setPassword(String password) {
                this.password = password;
            }
            
            @Override
            public void initialize(CeylonTool mainTool) {
            }
            
            @Override
            public void run() throws Exception {
                // TODO Auto-generated method stub
                
            }
        }
        
        @Description("Unsets the password for `<alias>` in the keystore, " +
        		"removing the alias and its corresponding password.")
        public class UnsetPassword implements Tool {
            
            private String alias;
            
            @Argument(argumentName="alias", multiplicity="1", order=1)
            public void setAlias(String alias) {
                this.alias = alias;
            }
            
            @Override
            public void initialize(CeylonTool mainTool) {
            }
            
            @Override
            public void run() throws Exception {
                // TODO Auto-generated method stub
                
            }
        }
        
        @Subtool(argumentName="action", classes={GetPassword.class, SetPassword.class, UnsetPassword.class})
        public void setAction(Tool action) {
            this.tool = action;
        }
        
        @Override
        public void initialize(CeylonTool mainTool) {
        }
        
        @Override
        public void run() throws Exception {
            tool.run();
        }
    }
    
    @Override
    public void initialize(CeylonTool mainTool) {
    }
    
    @Override
    public void run() throws Exception {
        action.run();
    }

    public static void main(String[] args) throws Exception {
        CeylonConfigTool x = new CeylonConfigTool();
        x.action = x.new Get();
        x.run();
    }
    
}

class RichConfigWriter extends ConfigWriter {

    @Override
    protected void writeSimpleSection(Writer writer, String section)
            throws IOException {
        super.writeSimpleSection(writer, OSUtil.color(section, OSUtil.Color.yellow));
    }

    @Override
    protected void writeCompoundSection(Writer writer, String pre, String post)
            throws IOException {
        super.writeCompoundSection(writer, OSUtil.color(pre, OSUtil.Color.yellow),
                OSUtil.color(post, OSUtil.Color.red));
    }

    @Override
    protected void writeOptionValue(Writer writer, String name, String value)
            throws IOException {
        super.writeOptionValue(writer, OSUtil.color(name, OSUtil.Color.blue), value);
    }
    
}
