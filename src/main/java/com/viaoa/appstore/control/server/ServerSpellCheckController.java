// Copied from OATemplate project by OABuilder 03/03/24 06:30 AM
package com.viaoa.appstore.control.server;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import com.viaoa.util.*;

public class ServerSpellCheckController {
    private static Logger LOG = Logger.getLogger(ServerSpellCheckController.class.getName());
    
    private TreeSet<String> tree;
    private TreeMap<String, String[]> treeSoundex;
    private TreeSet<String> treeNewWord;  // added by users, will store in separate file so that they can be added to master dictionary 

    public ServerSpellCheckController() {
        tree = new TreeSet<String>();
        treeSoundex = new TreeMap<String, String[]>();
        treeNewWord = new TreeSet<String>();
    }

    public void loadDictionaryTextFile(String fname) throws Exception {
        LOG.fine("dictionary text file is "+fname);
        _loadTextFile(fname, false);
    }
    
    public void loadNewWordsTextFile(String fname) throws Exception {
        LOG.fine("new words text file is "+fname);
        _loadTextFile(fname, true);
    }
    
    
    private void _loadTextFile(String fname, boolean bNewWords) throws Exception {
        if (fname == null) return;
        fname = OAString.convertFileName(fname);
        File file = new File(fname);
        if (!file.exists()) {
            LOG.fine("dictionary file \""+fname+"\" does not exist, program will continue with an empty dictionary");
            return;
        }
        FileReader inFile = new FileReader(file);
        BufferedReader in = new BufferedReader(inFile);
        
        long msBegin = System.currentTimeMillis();
        int i = 0;
        for ( ;;i++) {
            String word = in.readLine();
            if (word == null) break;
            word = convert(word.toLowerCase());
            // if ((i%100)==0) System.out.println(i+") "+word);
            if (bNewWords) {
                treeNewWord.add(word);
            }
            else {
                tree.add(word);
                String s = OAString.soundex(word);
                String[] ss = treeSoundex.get(s);
                ss = (String[]) OAArray.add(String.class, ss, word);
                treeSoundex.put(s, ss);
            }
        }
        long msEnd = System.currentTimeMillis();
        LOG.fine(i+" words loaded in  "+((msEnd-msBegin)/1e3) + " seconds, treeSize="+tree.size()+", soundexTreeSize="+treeSoundex.size() );
    }


    private Object LOCK_save = new Object();
    public void saveNewWordsTextFile(String fname) throws Exception {
        synchronized (LOCK_save) {
            LOG.fine("new words text file is "+fname);
            if (fname == null) return;
            fname = OAString.convertFileName(fname);
            FileWriter outFile = new FileWriter(fname);
            BufferedWriter out = new BufferedWriter(outFile);
            long msBegin = System.currentTimeMillis();
            
            Iterator<String> iter = treeNewWord.iterator();
            int i = 0;
            for ( ;iter.hasNext(); i++) {
                String word = iter.next();
                if (i > 0) out.newLine();
                out.write(word);
            }
            out.flush();
            out.close();
            outFile.close();
            long msEnd = System.currentTimeMillis();
            LOG.fine(i+" words loaded in  "+((msEnd-msBegin)/1e3) + " seconds, treeSize="+tree.size()+", soundexTreeSize="+treeSoundex.size() );
        }
    }    

    
    /**
     * Called when a client adds a new word.  Server will store this in a separate file from the master dictionary.
     * @param word
     */
    public synchronized void addNewWord(String word) {
        synchronized (LOCK_save) {
            if (word == null) return;
            // get alpha/numeric only
            word = word.toLowerCase();
            word = convert(word);
            
            treeNewWord.add(word);
        }
    }

    private String convert(String word) {
        int x = word.length();
        StringBuilder sb = null;
        for (int i=0; i<x; i++) {
            char c = word.charAt(i);
            if (!Character.isLetterOrDigit(c) && c != '-' && c != '.' && c != ' ') {
                if (sb == null) {
                    sb = new StringBuilder(word.length());
                    if (i > 0) sb.append(word.substring(0,i));
                }
            }
            else {
                if (sb != null) sb.append(c);
            }
        }
        if (sb == null) return word;
        return new String(sb);
    }
    
    public boolean isWordFound(String word) {
        if (word == null) return false;
        return tree.contains(word.toLowerCase());
    }

    /*
     * Find words that begin with text.
     * @param maxReturn max number of words to return
     * @return any words that begin with text, all return words will be in lowercase
     */
    public String[] getMatches(String word, int maxReturn) {
        if (word == null) return null;
        if (word.length() < 3) return null;
        word = word.toLowerCase();
        
        String[] ss = null;
        String s = tree.floor(word);
        for (int i=0; maxReturn<0 || i<maxReturn; i++) {
            if (s == null) break;
            if (!s.startsWith(word)) {
                if (s.compareTo(word) > 0) break;
            }
            else {
                ss = (String[]) OAArray.add(String.class, ss, s);
            }
            s = tree.higher(s);
        }
        return ss;
    }
    
    /**
     * Return soundex matches.
     */
    public String[] getSoundexMatches(String word) {
        if (word == null) return null;
        if (word.length() < 3) return null;
        String s = OAString.soundex(word);
        String[] ss = treeSoundex.get(s);
        return ss;
    }

    public static void main(String[] args) throws Exception {
        ServerSpellCheckController scc = new ServerSpellCheckController();
        scc.loadDictionaryTextFile("runtime\\server\\dictionary.txt");
        
        String[] ss = scc.getMatches("abhor", 99);
        if (ss != null) {
            for (String s : ss) {
                System.out.println(s);
            }
        }
        System.out.println("done");
    }
}
