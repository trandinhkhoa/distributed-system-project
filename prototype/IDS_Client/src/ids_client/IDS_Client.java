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
        if (args.length == 0){
            System.out.println("The client need an IP to connect to.");
        }
        
        String inputHash = "098f6bcd4621d373cade4e832627b4f6";
        
        try {
            md = java.security.MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(IDS_Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        Stack<String> work = new Stack();
        work.add("onfdche");
        work.add("onchdfe");
        work.add("test");
        work.add("oncsfhe");
        work.add("fonche");
        while (!work.isEmpty()){
            String currentWord = work.pop();
            String currentHash = bytesToHex(md.digest(currentWord.getBytes())).toLowerCase();
            if (currentHash.equals(inputHash)){
                System.out.println("Password found ! We have \"" + currentWord + "\" which is " + currentHash + ".");
                System.exit(0);
            }
            System.out.println(currentHash);
        }
        /*
Connect to the node
while (taskAreLeft && resultNotFound)
	Get a chunk
	result = Compute -> should be interruptable if result is found by someone else
	if (result == FOUND)
		send_found_to_server();
	else
		ask_server_more_work();
	fi
end
*/
    }
    
}
