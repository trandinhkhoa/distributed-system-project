import java.util.Stack;
import org.apache.commons.validator.routines.InetAddressValidator;

public class Client {
    private static boolean workToDo;
    private static boolean resultFound;

    private static Stack<String> chunk;

    public static void main (String[] args){
        // TODO: connect to load balancer
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
            System.out.println("[Client] Please enter a proper IP.");
            System.exit(1);
        }

        System.out.println(args[0]);

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
    }

    private static void getWork(){
        // TODO: request work from server
        // If the server send NULL, we have no more work to do.
        // If the server send something, we add it to our chunk.
    }

    private static void doWork(){
        // TODO: do the hash computing
        /*
        if (...){
            resultFound = true;
        }
        */

    }

    private static void sendResults(){
        // TODO: send result to out server
        if (resultFound == true){
            // TODO: send the result to the server
        } else {
            // TODO: request more work; not needed since we loop ?
        }
    }
}
