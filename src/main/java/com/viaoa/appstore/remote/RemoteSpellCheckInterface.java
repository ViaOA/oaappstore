// Copied from OATemplate project by OABuilder 03/03/24 06:30 AM
package com.viaoa.appstore.remote;

import com.viaoa.remote.multiplexer.annotation.OARemoteInterface;

@OARemoteInterface
public interface RemoteSpellCheckInterface {

    public final static String BindName = "RemoteSpellCheck";
    
    public String[] getMatchingWords(String word);
    public String[] getSoundexMatchingWords(String word);
    public void addNewWord(String word);
    public boolean isWordFound(String word);
    
}
