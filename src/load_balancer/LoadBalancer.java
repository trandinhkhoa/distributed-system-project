import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

import java.util.ArrayList;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.util.Stack;

public class LoadBalancer {

    private static String hashString;
    private static String hostFile;
    private static String dictionnaryFile;

    private static int bigChunkSize;
    private static int numberOfWords;
    private static int numberOfServers;

    private static Stack<Stack<String>> bigChunks = new Stack<>();

    private static ArrayList<String> serverList = new ArrayList<>();

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

        try {
            getServersInfo();
        } catch (FileNotFoundException e){
            System.err.println("Hostfile not found.\n" + e.getMessage());
            System.exit(1);
        } catch (IOException e){
            System.err.println("Unable to read from file:\n" + e.getMessage());
            System.exit(1);
        }

        try {
            splitDictionnary();
        } catch (FileNotFoundException e){
            System.err.println("Dictionnary file not found.\n" + e.getMessage());
            System.exit(1);
        } catch (IOException e){
            System.err.println("Unable to read from file:\n" + e.getMessage());
            System.exit(1);
        }

        distributeDictionnary();
        waitForClients();
    }

    private static void getServersInfo() throws FileNotFoundException, IOException{
        System.out.println("[LB] Reading host file...");
        BufferedReader bufferedReader = new BufferedReader(new FileReader(hostFile));
        String serverIp = bufferedReader.readLine();
        while (serverIp != null){
            serverList.add(serverIp);
            System.out.println(serverIp);
            serverIp = bufferedReader.readLine();
        }
        numberOfServers = serverList.size();
    }

    private static void splitDictionnary() throws IOException{
        BufferedReader reader = new BufferedReader(new FileReader(dictionnaryFile));
        System.out.println("[LB] Computing number of words...");
        while (reader.readLine() != null) numberOfWords++;
        reader.close();
        System.out.println("[LB] Num of words:" + numberOfWords);
        bigChunkSize = numberOfWords/numberOfServers;
        System.out.println("[LB] Maximum chunksize : " + bigChunkSize);

        System.out.println("[LB] Creating chunks...");

        reader = new BufferedReader(new FileReader(dictionnaryFile));
        String s = reader.readLine();
        Stack<String> stack = new Stack<>();
        while (s != null){
            if (stack.size() < bigChunkSize)
                stack.add(s);
            else{
                bigChunks.add(stack);
                stack = new Stack<>();
                stack.add(s);
            }
            // TODO: limit password size in the dictionnary ?
            s = reader.readLine();
        }
        bigChunks.add(stack);
    }

    private static void distributeDictionnary(){
        // TODO: put the file parts into a rabbitMQ queue
        // the servers will get the parts of the file
        // when the queue is empty, the function is over
        System.out.println("[LB] Distributing dictionnary...");
        System.out.println("[LB] TODO");
        // TODO: create a queue
        // when it's empty, it's over
        // A server connect to the queue, get one chunk
        // So it's not producer/consumer, more like reply stuff
        // Every server gets a different chunk

    }

    private static void waitForClients(){
        // TODO: wait for a client to connect, use a rabiitMQ queue ?
        System.out.println("[LB] Waiting for clients to connect...");
        System.out.println("[LB] TODO");
    }
}

