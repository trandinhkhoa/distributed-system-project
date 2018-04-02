/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package ids_client;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author whoami
 */
public class IDS_Client {
    
    private static MessageDigest md;
    private static Stack<String> work;
    
    /**
     * Convert an array of bytes into a string that can be compared.
     */
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        boolean resultFound = false;
        if (args.length == 0){
            System.out.println("The client need an IP to connect to.");
        }
        
        connectToServer(args[0]);
        
        String inputHash = "098f6bcd4621d373cade4e832627b4f6";
        inputHash = inputHash.toUpperCase();
        
        try {
            md = java.security.MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(IDS_Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        while ((work = getWorkFromServer()) != null){
            while (!work.isEmpty() && !resultFound){
                String currentWord = work.pop();
                String currentHash = bytesToHex(md.digest(currentWord.getBytes()));
                if (currentHash.equals(inputHash)){
                    System.out.println("Password found ! We have \"" + currentWord + "\" which is " + currentHash + ".");
                    resultFound = true;
                    sendResultToServer(currentWord);
                }
                System.out.println(currentHash);
            }
        }
    }
    
    /**
     * This will handle the connection, if needed.
     * @param arg 
     */
    private static void connectToServer(String arg) {
        
    }
    
    /**
     * When we found the input that correspond to the hash, we send it back to the server.
     * @param currentWord 
     */
    private static void sendResultToServer(String currentWord) {
        
    }

    /**
     * When we have finished the work sent by the server, we ask for more.
     * @return a stack of string to compute the hash of.
     */
    private static Stack<String> getWorkFromServer() {
        
        return null;
    }
    
}
