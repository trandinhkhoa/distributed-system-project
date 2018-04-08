import java.util.Stack;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.validator.routines.InetAddressValidator;

public class Client {
    private static boolean workToDo;
    private static boolean resultFound;

    private static String inputHash;
    private static String result;

    private static Stack<String> work = new Stack<>();
    private static MessageDigest md;
    private final static char[] hexDigits = "0123456789abcdef".toCharArray();

    /**
     * Convert an array of bytes into a string that can be compared.
     */
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++ ) {
            int d = bytes[i] & 0xFF;
            hexChars[i * 2] = hexDigits[d >>> 4];
            hexChars[i * 2 + 1] = hexDigits[d & 0x0F];
        }
        return new String(hexChars);
    }

    public static void main (String[] args){
        // TODO: connect to load balancer
        // get information
        // get work
        // do the work
        // send results
        // quit once no more work or result found

        if (args.length < 1){
            System.out.println("The client need an IP to connect to.");
            System.exit(0);
        }

        InetAddressValidator addressValidator = new InetAddressValidator();

        if (addressValidator.getInstance().isValidInet4Address(args[0]) == false){
            System.out.println("[Client] Please enter a proper IP address.");
            System.exit(1);
        }

        System.out.println("[Client] Connecting to " + args[0] + "...");

        // Get an message digest instance to compute a hash
        try {
            md = java.security.MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ex) {
            System.out.println("[Client] Unable to get an instance for that hashing algorithm.");
            System.exit(1);
        }

        connectToLoadBalancer();

        while(workToDo && !resultFound){
            getWork();
            doWork();
            sendResults();
        }
    }

    private static void connectToLoadBalancer(){
        // TODO: connect to the load balancer
        resultFound = false;
        workToDo = true;
        // TODO: get the hash to compute
        inputHash = "098f6bcd4621d373cade4e832627b4f6";
    }

    private static void getWork(){
        // TODO: request work from server
        // If the server send NULL, we have no more work to do.
        // If the server send something, we add it to our chunk.
        // TODO: FIXME this is a placeholder for the real getWork()
        workToDo = true;
        work.add("tessypoopoo");
        work.add("testessypoos");
        work.add("testessyputa");
        work.add("testessyraj");
        work.add("testessyrox");
        work.add("testessyru");
        work.add("testessys");
        work.add("testessys123");
        work.add("testessysaf");
        work.add("testessyscholtes");
        work.add("testessysurvire");
        work.add("testessyt");
        work.add("testessyta");
        work.add("testessytequiero");
        work.add("testessythecat");
        work.add("testessytwo");
        work.add("testessyu");
        work.add("testessyurace");
        work.add("testessyv");
        work.add("testessywessy");
        work.add("testessywessy1");
        work.add("testessyy");
        work.add("testessyybo");
        work.add("testessyysaulo");
        work.add("testessyyvette");
        work.add("testessy_12");
        work.add("testessy_wessy");
        work.add("testessz");
        work.add("testessza");
        work.add("testesszaloniki");
        work.add("test");
        work.add("testess_2007");
        work.add("testess_81");
        work.add("testess_angel");
        work.add("testess_a_belle_09");
        work.add("testess_f117");
        work.add("testess_g6902");
        work.add("testess_lady");
        work.add("testess_olivia");
        work.add("testess_zak");
        work.add("testesT#");
        work.add("testesT0815");
        work.add("testesT3ate");
        work.add("testest");
    }

    private static void doWork(){
        // TODO: do the hash computing
        while (!work.isEmpty() && !resultFound){
            String currentWord = work.pop();
            String currentHash = bytesToHex(md.digest(currentWord.getBytes()));
            if (currentHash.equals(inputHash)){
                result = currentWord;
                System.out.println("[Client] Password found ! We have \"" + currentWord + "\" which is " + currentHash + ".");
                resultFound = true;
            }
        }

    }

    private static void sendResults(){
        // TODO: send result to out server
        if (resultFound == true){
            //result;
            System.out.println("[Client] Result being sent is: " + result);
            // TODO: send the result to the server
        } else {
            // TODO: request more work; not needed since we loop ?
        }
    }
}
