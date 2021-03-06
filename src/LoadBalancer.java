import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

import java.util.ArrayList;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.util.Stack;

public class LoadBalancer {

    private static String hashString;
    private static String hostFile;
    private static String dictionaryFile;

    private static int bigChunkSize;
    private static int numberOfWords;
    private static int numberOfServers;

    private static Stack<Stack<String>> bigChunks = new Stack<>();

    private static ArrayList<String> serverList = new ArrayList<>();

    private static Connection connection;
    private static Channel channel;
    private static String DISTRIBUTE_QUEUE_NAME = "distribute_queue";

    public static void main(String [] args) throws Exception
    {
        if (args.length < 3){
            System.out.println("[LB] We need a MD5 hash, a dictionnary and the number of servers  as argument.");
            System.exit(0);
        }
        hashString = args[0];
        dictionaryFile = args[1];
        numberOfServers = Integer.parseInt(args[2]);
        System.out.println("[LB] MD5 hash: " + hashString);
        System.out.println("[LB] Dictionary file: " + dictionaryFile);
        System.out.println("[LB] Using " + numberOfServers + " server(s).");

        try {
            splitDictionnary();
        } catch (FileNotFoundException e){
            System.err.println("[LB] Dictionnary file not found.\n" + e.getMessage());
            System.exit(1);
        } catch (IOException e){
            System.err.println("[LB] Unable to read from file:\n" + e.getMessage());
            System.exit(1);
        }

        try {
            distributeDictionnary();
        } catch (IOException | TimeoutException | InterruptedException e) {
            e.printStackTrace();
        } finally {
        }
    }

    /**
     * Split a big dictionary into smaller chunks to be sent to the servers.
     * @throws IOException if the file doesn't exist
     */
    private static void splitDictionnary() throws IOException{
        BufferedReader reader = new BufferedReader(new FileReader(dictionaryFile));
        System.out.println("[LB] Computing number of words...");
        while (reader.readLine() != null) numberOfWords++;
        reader.close();
        System.out.println("[LB] Number of words:" + numberOfWords);
        bigChunkSize = numberOfWords/numberOfServers;
        System.out.println("[LB] Maximum chunksize : " + bigChunkSize);

        System.out.println("[LB] Creating chunks...");

        reader = new BufferedReader(new FileReader(dictionaryFile));
        String s = reader.readLine();
        Stack<String> stack = new Stack<>();
        while (s != null){
            if (stack.size() < bigChunkSize)
                stack.add(s);
            else{
                //bigChunk is the whole dictionary. Pop bigChunks to get a partitiion
                bigChunks.add(stack);
                stack = new Stack<>();
                stack.add(s);
            }
            s = reader.readLine();
        }
        if (!stack.empty()){
            bigChunks.add(stack);
        }
    }

    /**
     * Send the dictionary parts to all the servers that are there.
     * @throws Exception 
     */
    private static void distributeDictionnary() throws Exception{
        System.out.println("[LB] Distributing dictionnary");

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        connection = factory.newConnection();
        channel = connection.createChannel();
        channel.queueDeclare(DISTRIBUTE_QUEUE_NAME, false, false, false, null);

        for (int i = 0; i < numberOfServers; i++){
            Dictionary dictObj = new Dictionary(bigChunks.pop(), hashString, i, numberOfServers);
            channel.basicPublish("", DISTRIBUTE_QUEUE_NAME, null, dictObj.toBytes());
            System.out.println("[LB]  [x] Distribute the dictionary part " + dictObj.getNumber() );
        }

        //close communication after sent the request
        channel.close();
        connection.close();
    }

    /**
     * Wait for clients to connect to us.
     */
    private static void waitForClients(){
        System.out.println("[LB] Waiting for clients to connect...");
        System.out.println("[LB] TODO");
    }
}

