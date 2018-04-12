import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;

import java.util.ArrayList;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.util.Stack;

import org.apache.commons.validator.routines.InetAddressValidator;

public class LoadBalancer {

    private static String hashString;
    private static String hostFile;
    private static String dictionaryFile;

    private static int bigChunkSize;
    private static int numberOfWords;
    private static int numberOfServers = 3;

    private static Stack<Stack<String>> bigChunks = new Stack<>();

    private static ArrayList<String> serverList = new ArrayList<>();

    static Connection connection;
    static Channel channel;
    private static String DISTRIBUTE_QUEUE_NAME = "distribute_queue";

    public static void main(String [] args) throws Exception
    {
        // TODO: launch all the servers
        // put the parts of the file in a queue
        // the servers take their part from the queue

        // then we switch to lsiten mode for clients to connect

        // if (args.length < 3){
        if (args.length < 2){
            System.out.println("The load balancer need a MD5 hash, a host file and a dictionnary as argument.");
            System.exit(0);
        }
        //
        hashString = args[0];
        // hostFile = args[1];
        dictionaryFile = args[1];
        //
        // System.out.println("hash: " + hashString + " hostFile: " + hostFile + " dictionaryFile: " + dictionaryFile);
        System.out.println("hash: " + hashString + " dictionaryFile: " + dictionaryFile);
        //
        // try {
        //     getServersInfo();
        // } catch (FileNotFoundException e){
        //     System.err.println("Hostfile not found.\n" + e.getMessage());
        //     System.exit(1);
        // } catch (IOException e){
        //     System.err.println("Unable to read from file:\n" + e.getMessage());
        //     System.exit(1);
        // }
        //
        try {
            splitDictionnary();
        } catch (FileNotFoundException e){
            System.err.println("Dictionnary file not found.\n" + e.getMessage());
            System.exit(1);
        } catch (IOException e){
            System.err.println("Unable to read from file:\n" + e.getMessage());
            System.exit(1);
        }

        try {
            distributeDictionnary();
        } catch (IOException | TimeoutException | InterruptedException e) {
            e.printStackTrace();
        } finally {
        }
    }

    private static void getServersInfo() throws FileNotFoundException, IOException{
        System.out.println("[LB] Reading host file...");
        BufferedReader bufferedReader = new BufferedReader(new FileReader(hostFile));
        InetAddressValidator addressValidator = new InetAddressValidator();

        String serverIp = bufferedReader.readLine();
        while (serverIp != null){

            if (addressValidator.getInstance().isValidInet4Address(serverIp) == true){
                serverList.add(serverIp);
            } else {
                System.out.println("[LB] Error processing IP \""+serverIp+"\" is not a valid IPv4 address.");
            }
            System.out.println(serverIp);
            serverIp = bufferedReader.readLine();
        }
        numberOfServers = serverList.size();
    }

    private static void splitDictionnary() throws IOException{
        BufferedReader reader = new BufferedReader(new FileReader(dictionaryFile));
        System.out.println("[LB] Computing number of words...");
        while (reader.readLine() != null) numberOfWords++;
        reader.close();
        System.out.println("[LB] Num of words:" + numberOfWords);
        
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
            // TODO: limit password size in the dictionnary ?
            s = reader.readLine();
        }
        if (!stack.empty()){
            bigChunks.add(stack);
        }
    }

    private static void distributeDictionnary() throws Exception{
        // TODO: put the file parts into a rabbitMQ queue
        // the servers will get the parts of the file
        // when the queue is empty, the function is over
        System.out.println("[LB] Distributing dictionnary");
        // TODO: create a queue
        // when it's empty, it's over
        // A server connect to the queue, get one chunk
        // So it's not producer/consumer, more like reply stuff
        // Every server gets a different chunk

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        connection = factory.newConnection();
        channel = connection.createChannel();
        channel.queueDeclare(DISTRIBUTE_QUEUE_NAME, false, false, false, null);

        for (int i = 0; i < numberOfServers; i++){
            Dictionary dictObj = new Dictionary(bigChunks.pop(), hashString, i);
            channel.basicPublish("", DISTRIBUTE_QUEUE_NAME, null, dictObj.toBytes());
            System.out.println(" [x] Distribute the dictionary part " + dictObj.getNumber() );
        }
        
        //close communication after sent the request 
        channel.close();
        connection.close();
    }


    private static void waitForClients(){
        // TODO: wait for a client to connect, use a rabiitMQ queue ?
        System.out.println("[LB] Waiting for clients to connect...");
        System.out.println("[LB] TODO");
    }
}

