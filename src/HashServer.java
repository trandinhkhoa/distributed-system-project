import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

import java.util.Stack;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class HashServer {

    private static String hashString;
    private static String loadBalancerIp;

    private static Connection connection;
    private static Channel channel;
    private static String requestQueueName = "rpc_queue";
    private static String replyQueueName;

    private static Stack<String> bigChunk = new Stack<>();
    private static Stack<Stack<String>> chunks = new Stack<>();
    private static final Integer chunkSize = 100000;

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
        System.out.println("[Server] hash: " + hashString);
        System.out.println("[Server] loadBalancerIp: " + loadBalancerIp);
        // TODO : how do the server can communicate with each others ?
        // a ring yes, but how do they know where it is ?

        try {
            getDictionnaryPart();
        } catch (IOException | TimeoutException e){
            System.out.println("[Server] Error obtaining dictionnary part.");
            System.out.println(e.getMessage());
            System.exit(1);
        }

        splitDictionnary();

        waitForClients();
        propagateResults();
    }

    public static void getDictionnaryPart() throws IOException, TimeoutException{
        // TODO: get dictionnary part from the load balancer

        System.out.println("[Server] Obtaining dictionary part");
        // Connection to the load balancer
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(loadBalancerIp);

        connection = factory.newConnection();
        channel = connection.createChannel();

        replyQueueName = channel.queueDeclare().getQueue();

        channel.queueDeclare(SEND_WORK_QUEUE_NAME, false, false, false, null);


    }

    public static void waitForClients(){
        // TODO: wait for connection, when  work is done,
        // or when someone found the result, we propagate them

        // 4 types of requests
        // NEW : send a stack with one string: the hash
        // WORK : send a stack with work to do
        // RESULT : propagate result, stop wating

        // We send :
        // null : no more work to do
    }

    public static void propagateResults(){
        // TODO: we send to the other servers the result of our clients
        // is it our job, or the LB job ?
    }

    public static void splitDictionnary(){
        Stack<String> smallerChunk = new Stack<>();
        while (!bigChunk.empty()){
            if (chunks.size() < chunkSize){
                smallerChunk.push(bigChunk.pop());
            } else {
                chunks.push(smallerChunk);
                smallerChunk = new Stack<>();
                smallerChunk.push(bigChunk.pop());
            }
        }

        if (!smallerChunk.empty()){
            chunks.push(smallerChunk);
        }
    }
}
