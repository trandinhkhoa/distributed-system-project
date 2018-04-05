
public class LoadBalancer {

    private static String hashString;
    private static String hostFile;
    private static String dictionnaryFile;

    public static void main(String [] args)
    {
        // TODO: launch all the servers
        // put the parts of the file in a queue
        // the servers take their part from the queue

        // then we switch to lsiten mode for clients to connect

        if (args.length < 3){
            System.out.println("The load balancer need a MD5 hash, a host file and a dictionnary as argument.");
            System.exit(0);
        }

        hashString = args[0];
        hostFile = args[1];
        dictionnaryFile = args[2];

        System.out.println("hash: " + hashString + " hostFile: " + hostFile + " dictionnaryFile: " + dictionnaryFile);

        splitDictionnary();
        launchServers();
        distributeDictionnary();
        waitForClients();
    }

    private static void launchServers(){
        // TODO: we go through host file, and we launch each servers
    }

    private static void splitDictionnary(){
        // TODO: open and split dictionnary file
    }

    private static void distributeDictionnary(){
        // TODO: put the file parts into a rabbitMQ queue
        // when the queue is empty, the function is over
    }

    private static void waitForClients(){
        // TODO: wait for a client to connect, use a rabiitMQ queue ?
    }
}

