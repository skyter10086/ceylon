/********************************************************************************
 * Copyright (c) 2011-2017 Red Hat Inc. and/or its affiliates and others
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Apache License, Version 2.0 which is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: Apache-2.0 
 ********************************************************************************/
package org.eclipse.ceylon.cmr.ceylon;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.eclipse.ceylon.cmr.api.ArtifactContext;
import org.eclipse.ceylon.cmr.api.RepositoryManager;
import org.eclipse.ceylon.common.log.Logger;

/*
 * More or less copied from the compiler's ShaSigner :(
 */
public class ShaSigner {
    
    public static String sha1(File jarFile, Logger log) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            // can't happen, specs say SHA-1 must be implemented
            log.warning("Failed to get a SHA-1 message digest, your JRE does not follow the specs. "
                    +"No SHA-1 signature will be made");
            return null;
        }
        FileInputStream is;
        try {
            is = new FileInputStream(jarFile);
        } catch (FileNotFoundException e) {
            // can't happen since we just created the file
            log.warning("Failed to open archive file "+jarFile.getPath()
                    +", no SHA-1 signature will be made");
            return null;
        }
        byte[] buffer = new byte[1024];
     
        int read = 0; 
        try {
            while ((read = is.read(buffer)) != -1) {
              digest.update(buffer, 0, read);
            }
        } catch (IOException e) {
            log.warning("Failed to read archive file "+jarFile.getPath()
                    +", no SHA-1 signature will be made");
            return null;
        }finally{
            try {
                is.close();
            } catch (IOException e) {
                // don't care
            }
        }
        return toHexString(digest.digest());
    }

    final static char[] Hexadecimal = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};

    private static String toHexString(byte[] bytes){
        char[] chars = new char[bytes.length*2];
        for(int b=0,c=0;b<bytes.length;b++){
            int v = (int)bytes[b] & 0xFF;
            chars[c++] = Hexadecimal[v/16];
            chars[c++] = Hexadecimal[v%16];
        }
        return new String(chars);
    }

    
    public static File writeSha1(String sha1, Logger log) {
        File shaFile;
        try{
            shaFile = File.createTempFile("ceylon-signer-", ".sha1");
        }catch (IOException x){
            log.warning("Failed to create temporary file for the SHA-1 signature"
                    +", no SHA-1 signature will be made");
            return null;
        }
        OutputStream os;
        try {
            os = new FileOutputStream(shaFile);
        } catch (FileNotFoundException e) {
            log.warning("Failed to open archive file "+shaFile
                    +" for writing, no SHA-1 signature will be made");
            shaFile.delete();
            return null;
        }
        try {
            os.write(sha1.getBytes("ASCII"));
            return shaFile;
        } catch (UnsupportedEncodingException e) {
            log.warning("Failed to get an ASCII charset, your JRE does not follow the specs. "
                    +"No SHA-1 signature will be made");
            shaFile.delete();
            return null;
        } catch (IOException e) {
            log.warning("Failed to write to "+shaFile.getPath()
                    +", no SHA-1 signature will be made");
            shaFile.delete();
            return null;
        }finally{
            try {
                os.flush();
                os.close();
            } catch (IOException e) {
                // don't care
            }
        }
    }

    public static void signArtifact(RepositoryManager repoman, ArtifactContext context, File jarFile, Logger log){
        ArtifactContext sha1Context = context.getSha1Context();
        if (sha1Context != null) {
            sha1Context.setForceOperation(true);
            String sha1 = sha1(jarFile, log);
            if(sha1 != null){
                File shaFile = writeSha1(sha1, log);
                if(shaFile != null){
                    try{
                        repoman.putArtifact(sha1Context, shaFile);
                    }finally{
                        shaFile.delete();
                    }
                }
            }
        }
    }

}