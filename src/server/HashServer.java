public class HashServer {

    private static String hashString;
    private static String loadBalancerIp;

    public static void main (String[] args){
        // TODO: bootstrap
        // we get the dictionnary from the load balancer
        //
        // then we wait for incoming connections
        // servers communicate between themselves with rings

        if (args.length < 2){
            System.out.println("A server need a MD5 hash, and a rabbitMQ IP to connect to.");
            System.exit(0);
        }

        hashString = args[0];
        loadBalancerIp = args[1];

        System.out.println("[Server] Starting...");
        // TODO : how do the server can communicate with each others ?
        // a ring yes, but how do they know where it is ?

        getDictionnaryPart();
        waitForClients();
        propagateResults();
    }

    public static void getDictionnaryPart(){
        // TODO: get dictionnary part from the load balancer

    }

    public static void waitForClients(){
        // TODO: wait for connection, when  work is done,
        // or when someone found the result, we propagate them
    }

    public static void propagateResults(){
        // TODO: we send to the other servers the result of our clients
        // is it our job, or the LB job ?
    }
}
