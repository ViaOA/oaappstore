// Copied from OATemplate project by OABuilder 03/03/24 06:30 AM
package com.viaoa.appstore.control.client;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import com.viaoa.appstore.delegate.RemoteDelegate;
import com.viaoa.util.*;

/**
 * Works with ServerDelegate to manage trees of words and soundexes.
 * Has local dictionary of words that user has added.
 * @author vvia
 */
public class ClientSpellCheckController {
    private static Logger LOG = Logger.getLogger(ClientSpellCheckController.class.getName());
    
    // lazy loaded from server using ServerDelegate
    private TreeSet<String> tree = new TreeSet<String>();
    private TreeMap<String, String[]> treeSoundex = new TreeMap<String, String[]>();

    // found words on server
    private TreeSet<String> treeFound = new TreeSet<String>();
    
    
    // not found on server, stored so that they will not be requested from server again
    private TreeSet<String> treeNotFound  = new TreeSet<String>();;
    private TreeSet<String> treeMatchesNotFound  = new TreeSet<String>();;
    private TreeSet<String> treeSoundexNotFound  = new TreeSet<String>();;

    // Local user only, stored in local file
    private TreeSet<String> treeLocal = new TreeSet<String>();
    private TreeMap<String, String[]> treeLocalSoundex = new TreeMap<String, String[]>();

    /**
     * Save loacally created words.
     */
    public void loadLocalNewWordsTextFile(String fname) throws Exception {
        LOG.fine("new words text file is " + fname);
        if (fname == null) return;
        fname = OAString.convertFileName(fname);
        File file = new File(fname);
        if (!file.exists()) {
            LOG.fine("file does not exist, will continue");
            return;
        }
        FileReader inFile = new FileReader(file);
        BufferedReader in = new BufferedReader(inFile);
        long msBegin = System.currentTimeMillis();
        int i = 0;
        for ( ;;i++) {
            String word = in.readLine();
            if (word == null) break;
            addNewWord(word, false);
        }
        long msEnd = System.currentTimeMillis();
        LOG.fine(i+" words loaded in  "+((msEnd-msBegin)/1e3) + " seconds, treeSize="+tree.size()+", soundexTreeSize="+treeSoundex.size() );
    }
    
    private Object LOCK_save = new Object();
    public void saveLocalNewWordsTextFile(String fname) throws Exception {
        synchronized (LOCK_save) {
            LOG.fine("new words text file is "+fname);
            if (fname == null) return;
            fname = OAString.convertFileName(fname);
            FileWriter outFile = new FileWriter(fname);
            BufferedWriter out = new BufferedWriter(outFile);
            long msBegin = System.currentTimeMillis();
            
            Iterator<String> iter = treeLocal.iterator();
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
     * Called by SpellCheck UI when the user would like to add a word to the dictionary.
     */
    public void addNewWord(String word) {
        synchronized (LOCK_save) {
            addNewWord(word, true);
        }
    }
    
    private void addNewWord(String word, boolean bSendToServer) {
        if (word == null) return;
        // get alpha/numeric only
        word = word.toLowerCase();
        word = convert(word);
        
        treeLocal.add(word);

        String soundx = OAString.soundex(word);
        
        String[] ss = treeLocalSoundex.get(soundx);
        ss = (String[]) OAArray.add(String.class, ss, word);
        treeLocalSoundex.put(soundx, ss);

        if (bSendToServer) {
            RemoteDelegate.getRemoteSpellCheck().addNewWord(word);
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
        word = word.toLowerCase();
        if (tree.contains(word)) return true;  // built by getMatches()
        if (treeFound.contains(word)) return true;  // from previous isWordFound()
        if (treeLocal.contains(word)) return true;  // locally added words
        if (treeNotFound.contains(word)) return false;  // not found on server

        boolean b = RemoteDelegate.getRemoteSpellCheck().isWordFound(word);
        if (b) treeFound.add(word); // dont add to local tree, since it would only add one word, and not other words that with same spelling
        else treeNotFound.add(word);
        return b;
    }

    /*
     * Find words that begin with text.
     * @param maxReturn max number of words to return
     * @return any words that begin with text, all return words will be in lowercase
     */
    public String[] getMatches(String word, int maxReturn) {
        if (word == null) return null;
        word = word.toLowerCase();
        if (word.length() < 3) {
            if (word.trim().length() == 0) return null;
            return new String[] {word};
        }
        
        String[] ss = null;
        if (!treeMatchesNotFound.contains(word)) {
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
            
            if (ss == null) {
                ss = RemoteDelegate.getRemoteSpellCheck().getMatchingWords(word);
                if (ss == null || ss.length == 0) {
                    treeMatchesNotFound.add(word);
                }
                else {
                    // add to cache
                    for (String sx : ss) {
                        tree.add(sx);  // dont add to soundex, since we dont have the full soundex list
                    }
                }
            }
        }
        
        // add any local
        boolean b = false;
        String s = treeLocal.floor(word);
        for (int i=0; maxReturn<0 || i<maxReturn; i++) {
            if (s == null) break;
            if (!s.startsWith(word)) {
                if (s.compareTo(word) > 0) break;
            }
            else {
                ss = (String[]) OAArray.add(String.class, ss, s);
                b = true;
            }
            s = treeLocal.higher(s);
        }
        if (b) Arrays.sort(ss);
        
        return ss;
    }
    
    /**
     * Return soundex matches.
     */
    public String[] getSoundexMatches(String word) {
        if (word == null) return null;
        if (word.length() < 3) return null;
        word = word.toLowerCase();
        String soundx = OAString.soundex(word);

        String[] ss = treeSoundex.get(soundx);
        if (ss != null) return ss;
        
        if (treeSoundexNotFound.contains(soundx)) return null;
        
        ss = RemoteDelegate.getRemoteSpellCheck().getSoundexMatchingWords(word);
        if (ss == null || ss.length == 0) {
            treeSoundexNotFound.add(word);
        }
        else {
            treeSoundex.put(soundx, ss);
        }

        String[] ss2 = treeLocalSoundex.get(soundx);
        if (ss2 != null) {
            for (String s : ss2) {
                ss = (String[]) OAArray.add(String.class, ss, s);
            }
            Arrays.sort(ss);
        }        
        return ss;
    }

    public static void main(String[] args) throws Exception {
        ClientSpellCheckController scc = new ClientSpellCheckController();
        
        String[] ss = scc.getMatches("abhor", 99);
        if (ss != null) {
            for (String s : ss) {
                System.out.println(s);
            }
        }
        System.out.println("done");
        
    }
}
