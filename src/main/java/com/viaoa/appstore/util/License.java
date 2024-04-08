// Copied from OATemplate project by OABuilder 03/03/24 06:30 AM
package com.viaoa.appstore.util;

import java.io.*;
import java.util.*;

import com.viaoa.util.*;

/**
    Used to create and validate license strings based on the following format:
    Used internally by LicenseController
    <pre>
    
    SiteId '-' StartupType '-' expirationDate '-' CheckDigitCode '-' NicCode
    
    SiteId: unique identifier used for the site.
    StartupType: 1:Single 2:Server 3:Client 4:Demo 5:Service
    ExpirationDate: MMddyy
    CheckDigitCode:  sum(all digits in license) % 17
    NicCode: 4 char code based on "Physical Address" returned from "configip /all" DOS command 
    </pre>

 	@author Vince Via
 	@version revised Aug 12, 2007
*/
public class License {

 	// The various modes that software can run as. 
	public static final int STARTUP_UNKNOWN  = 0;
 	public static final int STARTUP_SINGLE   = 1;
	public static final int STARTUP_SERVER   = 2;
 	public static final int STARTUP_CLIENT   = 3;
 	public static final int STARTUP_DEMO     = 4;
	public static final int STARTUP_SERVICE  = 5;
	public static final int STARTUP_MAX      = 5;
	
	public static final String[] STARTUP_TYPES      = new String[] { "Unknown", "Single", "Server", "Client", "Demo", "Service" };  // keep single word
	public static final String[] STARTUP_TYPES_LONG = new String[] { "Choose Version", "Single User Version", "Server Version", "Client Version", "Demo Version", "Web Service" };
	
	
    /** License is valid.  @see #isValid */
    public static final int VALID_LICENSE = 0;

    /** No license set.  @see #isValid */
    public static final int INVALID_NO_LICENSE = 1;

    /** Network ID Code does not match this computer.  @see #isValid */
    public static final int INVALID_NIC = 2;

    /** Bad check digit.  @see #isValid */
    public static final int INVALID_CHECKDIGIT = 3;

    /** License has expired.  @see #isValid */
    public static final int INVALID_EXPIRED = 4;

    /** Must be valid startup type from Startup */
    public static final int INVALID_STARTUP = 5;

    
    protected static String[] nicAddresses;



    /**
        Verifies that license string.
        @return 0 if valid, otherwise one of the Error Codes.
    */
    public static int getValid(String license) {

        if (license == null) return INVALID_NO_LICENSE;
        
        String guid;
        String type;
        String sDate;
        String checkDigit;
        String nicCode;

        guid = OAString.field(license, "-", 1);
        type = OAString.field(license, "-", 2);
        
        if (type == null || !OAString.isNumber(type) || OAConv.toInt(type) <= 0 || OAConv.toInt(type) > STARTUP_MAX) { 
        	return INVALID_STARTUP;
        }
        
        sDate = OAString.field(license, "-", 3);
        
        // must remove random characters from check digit
        String s = OAString.field(license, "-", 4); // check digit
        checkDigit = "";        
        for (int i=0; s != null && i < s.length(); i++) {
            if ( Character.isDigit(s.charAt(i)) ) checkDigit += s.charAt(i);
        }
        nicCode = OAString.field(license, "-", 5);
        if (nicCode == null) nicCode = "";
        
        int chk = 0;
        s = guid+type+sDate+nicCode;
        for (int i=0; s != null && i<s.length(); i++) {
            if ( Character.isDigit(s.charAt(i)) ) chk += s.charAt(i) - '0';
        }
        chk = chk % 17;

        if (checkDigit == null || !checkDigit.equals(chk+"")) return INVALID_CHECKDIGIT;

        OADate expDate = null;
        if (sDate != null && sDate.length() > 3) {  // "25" is used for no expiration date
            expDate = (OADate) OADate.valueOf(sDate, "MMddyy");
            if (expDate == null || expDate.isBefore(new OADate())) return INVALID_EXPIRED;
        }

        // Do this last, so that we can know that everything else was correct.
        // make sure that the NIC code is valid for this computer
        if (nicCode == null || !isValidNicCode(nicCode)) {
            if (nicCode == null || !nicCode.equalsIgnoreCase("NICK")) {
                return INVALID_NIC;
            }
        }
        
        return 0; // valid
    }

    
    public static OADate getExpirationDate(String license) {
    	if (license == null) return null;
        String s = OAString.field(license, "-", 3);
        if (s == null || s.length() < 6) return null;
        
        OADate d = (OADate) OADate.valueOf(s, "MMddyy");
        return d;
    }

    /**
        Returns the module 17 for the sum of all of the digits in a string.
    */
    protected static int calcCheckDigit(String s) {
        int chk = 0;
        for (int i=0; s != null && i<s.length(); i++) {
            if ( Character.isDigit(s.charAt(i)) ) chk += s.charAt(i) - '0';
        }
        chk = chk % 17;
        return chk;
    }

    /** 
        Creates a valid license for a machine with a specific NIC code.
        @param guid unique identifier assigned for use by database.
        @param type 1=single, 2=sever, 3=client, 4=Demo
        @param expDate expiration/timeout date.  Can be null for "no expiration"
        @param nicCode NIC code generated on machine that needs a license.
        @see Application
    */
    public static String createLicense(String guid, int type, OADate expDate, String nicCode) {
        String s = guid;
        s += type;
        if (expDate == null) s += "25";
        else s += expDate.toString("MMddyy");
        s += nicCode;
        
        int chk = calcCheckDigit(s);
        
        // create license
        String license = guid + "-";
        license += type + "-";

        if (expDate == null) license += ""; 
        else license += expDate.toString("MMddyy");

        license += '-';
        license += (char) getRandomChar();
        license += chk;
        license += (char) getRandomChar();

        license += '-' + nicCode;
        
        return license;
    }
    
    /** Generate a random character. */
    static char getRandomChar() {
        char c = (char) ('A' + (Math.random() * 25) ); 
        if (c == 'O') c = 'E';
        if (c == 'B') c = 'F';
        if (c == 'Z') c = 'W';
        return c;
    }


    
    /**
        Returns a 4 character code based on the NIC Address.
        @see #getNicAddress
    */
    public static String getNicAddressCode(String nic) {
    	String nicAddressCode;

        StringTokenizer st = new StringTokenizer(nic, "-");
        String code = "";
        for (int i=0;st.hasMoreElements();i++) {
            String s = st.nextToken();
            if (s.length() < 2) code += "X";
            else code += s.charAt((i%2));
        }
        int x = 0;
        for (int i=0; i<code.length(); i++) {
            x += code.charAt(i);
        }
        code = ((char) ('A' + (x%26))) + code;
        code = code.substring(0,4);
        nicAddressCode = "";
        for (int i=0; i<code.length(); i++) {
            char c = code.charAt(i);
            if (c == 'O') c = '3';
            nicAddressCode += c;
        }
        return nicAddressCode;
    }
    

    public static String getFirstNicAddress() {
    	String[] ss = getNicAddresses();
    	if (ss == null || ss.length == 0) return null;
    	return ss[0];
    }    

    
    /**
        Returns the Address for the Network Identifier Card by calling "ipconfig /all"
        and capturing the all "Physical Address" lines.
    */
    public static String[] getNicAddresses() {
        if (nicAddresses != null) return nicAddresses;
        try {
            InputStream is = Runtime.getRuntime().exec("ipconfig /all").getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String find = "Physical Address".toUpperCase();
            for (;;) {
                String line = br.readLine();
                if (line == null) break;
                int pos = line.toUpperCase().indexOf(find);
                if (pos < 0) continue;
                pos = line.indexOf('-', pos);
                if (pos < 0) continue;
                pos -= 2;
                int pos2 = line.indexOf(pos, ' ');
                if (pos2 < 0) pos2 = line.length();
                line = line.substring(pos, pos2);
                if (nicAddresses == null) nicAddresses = new String[1];
                else {
                	String[] ss = new String[nicAddresses.length + 1];
                	System.arraycopy(nicAddresses, 0, ss, 0, nicAddresses.length);
                	nicAddresses = ss;
                }
                nicAddresses[nicAddresses.length-1] = line;
            }        
        }
        catch (Exception e) {
            nicAddresses = null;
        }
        if (nicAddresses == null) nicAddresses = new String[] {"01-A-3B-AC-89-32"};
        return nicAddresses;
    }
    
    /**
     * Verifies that a NicCode is valid on this workstation.
     */
    public static boolean isValidNicCode(String code) {
    	if (code == null) return false;
    	String[] ss = getNicAddresses();
    	for (int i=0; ss != null && i<ss.length; i++) {
    		String s = getNicAddressCode(ss[i]);
    		if (code.equalsIgnoreCase(s)) return true;
    	}
    	return false;
    }


    public static int getStartupType(String license) {
    	if (license == null) return -1;
        String s = OAString.field(license, "-", 2);
        if (s == null) return -1;
        int x =  OAConv.toInt(s);
        if (x < 0 || x > STARTUP_MAX) x = STARTUP_UNKNOWN;
        return x;
    }
    public static String getGuid(String license) {
    	if (license == null) return null;
        return OAString.field(license, "-", 1);
    }
    public static String getNicCode(String license) {
    	if (license == null) return null;
        return OAString.field(license, "-", 5);
    }
    public static String getCheckCode(String license) {
    	if (license == null) return null;
        return OAString.field(license, "-", 4);
    }
    
    /**
     * The integer value of the Check Code.
     * Used to match against the sum of all other license digits mod 17
     * @see #getSumUsedForCheckDigit to get sum of all other digits in license code.
     */
    public static int getCheckCodeInt(String license) {
    	if (license == null) return -1;
        String s = OAString.field(license, "-", 4); // check digit
        String checkDigit = "";        
        for (int i=0; s != null && i < s.length(); i++) {
            if ( Character.isDigit(s.charAt(i)) ) checkDigit += s.charAt(i);
        }
        return OAConv.toInt(checkDigit);
    }

    /**
     * The sum of all digits in a license code, excluding the CheckCode. 
     * @see #getCheckCodeInt
     */
   public static int getSumUsedForCheckDigit(String license) {
        String guid;
        String type;
        String sDate;
        String nicCode;

        guid = OAString.field(license, "-", 1);
        type = OAString.field(license, "-", 2);
        sDate = OAString.field(license, "-", 3);
        nicCode = OAString.field(license, "-", 5);
        int chk = 0;
        String s = guid+type+sDate+nicCode;
        for (int i=0; s != null && i<s.length(); i++) {
            if ( Character.isDigit(s.charAt(i)) ) chk += s.charAt(i) - '0';
        }
        return chk;
    }
    
    
   /**
    * Creates a readable description for a license code.
    */
   public static String getDescription(String license) {
    	if (license == null) return null;

    	int x = getStartupType(license);
    	if (x < 0 || x > STARTUP_MAX) x = STARTUP_UNKNOWN;

    	String s = STARTUP_TYPES[x];
    	
    	if (x != STARTUP_CLIENT && x != STARTUP_DEMO) {
    		OADate d = getExpirationDate(license);
    		if (d != null) s += ", expires " + d.toString();
    		s += ", site " + getGuid(license); 
    		s += ", computerId " + getNicCode(license); 
    	}
    	return s;
    }

    
    /**
     * Used as login and license "backdoor" password.
     * For login, need to use userName "admin"
     * For license code, use this for the CheckDigitCode/AccessCode
     * Based on mod 5 for day of month.
     * A0Z, B1Y, C2X, D3W, E4V
     * @returns 3 char code, where all alpha are uppercase
     */
    public static String getBackdoorCode() {
    	return getBackdoorCode(5);
    }
    /**
     * Used as login and license "backdoor" password.
     * For login, need to use userName "admin"
     * Based on mod for day of month.
     * @returns 3 char code, where all alpha are uppercase
     */
    public static String getBackdoorCode(int mod) {
    	OADate d = new OADate();
        int chkdig = d.getDay() % mod;
        String s = (new Character((char)('A' + chkdig))) + "" + chkdig + "" + (new Character((char)('Z' - chkdig)));
        return s;
    }

}       

