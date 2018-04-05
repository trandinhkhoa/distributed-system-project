public class Client {

    public static void main (String[] args){
        // TODO: connect to load balancer
        // get work
        // do the work
        // send results
        // quit once no more work or result found

        if (args.length < 1){
            System.out.println("This need an IP to connect to.");
        }

        connectToLoadBalancer();
        while(workToDo && !resultFound){
            getWork();
            sendResults();
        }
    }

    private static void connectToLoadBalancer(){
        // TODO: connect to the load balancer
    }

    private static void getWork(){
        // TODO: request work from server
    }

    private static void sendResults(){
        // TODO: send result to out server
    }
}
