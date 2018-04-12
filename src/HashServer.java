import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;

import java.util.Stack;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.validator.routines.InetAddressValidator;

import java.util.Random;

public class HashServer {

    private static String hashString;
    private static String loadBalancerIp;

    //EXPLAIN: declare queue REQUEST_QUEUE_NAME used to receive request 
    private final static String REQUEST_QUEUE_NAME = "request_queue";
    private final static String DISTRIBUTE_QUEUE_NAME = "distribute_queue";
    
    // private static Message myPartition = new Message("NOTHING"); //placeholder, this is the part of the dictionary the server receveive from the LB
    private static Dictionary myPartition = new Dictionary(0); //placeholder, this is the part of the dictionary the server receveive from the LB

    // private static Stack<String> bigChunk = new Stack<>();
    private static Stack<Stack<String>> chunks = new Stack<>();
    private static final Integer chunkSize = 100000;

    public static void main (String[] args){
        // TODO: bootstrap
        // we get the dictionnary from the load balancer
        //
        // then we wait for incoming connections
        // servers communicate between themselves with rings

        // if (args.length < 2){
        //     System.out.println("A server need a MD5 hash, and a rabbitMQ IP to connect to.");
        //     System.exit(0);
        // }
        //
        // hashString = args[0];
        // loadBalancerIp = args[1];
        //
        // InetAddressValidator addressValidator = new InetAddressValidator();
        // if (addressValidator.getInstance().isValidInet4Address(loadBalancerIp) == false){
        //     System.out.println("[Server] Please enter a proper IP address.");
        //     System.exit(1);
        // }
        
        // System.out.println("[Server] Starting...");
        // System.out.println("[Server] hash: " + hashString);
        // System.out.println("[Server] loadBalancerIp: " + loadBalancerIp);
        //
        // TODO : how do the server can communicate with each others ?
        // a ring yes, but how do they know where it is ?

        try {
            getDictionnaryPart();
        } catch (IOException | TimeoutException e){
            System.out.println("[Server] Error obtaining dictionnary part.");
            System.out.println(e.getMessage());
            System.exit(1);
        }


        //Wait and send work
        //TODO
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        Connection connection = null;
        try {
            waitForClients();
        } catch (IOException | TimeoutException e) {
            System.out.println("[Server] Error while waiting for clients.");
            System.out.println(e.getMessage());
            System.exit(1);
        } finally {
            if (connection != null)
                try {
                    connection.close();
                } catch (IOException _ignore) {}
        }
        
        // propagateResults();
    }

    public static void getDictionnaryPart() throws IOException, TimeoutException{
        // TODO: get dictionnary part from the load balancer
        ConnectionFactory factory = new ConnectionFactory();
        // factory.setHost(loadBalancerIp);
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(DISTRIBUTE_QUEUE_NAME, false, false, false, null);
        System.out.println(" [*] Waiting for My Dictionary Partition. To exit press CTRL+C");

        // final Message[] msgObj_reply = new Message[1];

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                Dictionary dictObj = Dictionary.fromBytes(body);
                System.out.println(" [x] Received partition '" + dictObj.getNumber() + "'");
                try{
                    storePartition(dictObj);
                } catch (Exception e) {
                    e.printStackTrace();
                } 
                // msgObj_reply[0] = Message.fromBytes(body);
                // System.out.println(" [x] Received '" + msgObj_reply[0].getMsg() + "'");
            }
        };
        channel.basicConsume(DISTRIBUTE_QUEUE_NAME, true, consumer);
    }

    public static void storePartition(Dictionary partition) throws Exception{
        myPartition = partition; 
        System.out.println(" [x] Saved my partition '" + myPartition.getNumber() + "'");

        Stack<String> stack = myPartition.getDict();

        // int count = 0;
        // while ((!stack.empty()) && (count < 10))
        // {
        //     System.out.println("Testing... size of small stack is " + stack.size() + " example element = " + stack.pop());
        //     count++;
        // }

        splitDictionnary();
    }

    public static void waitForClients() throws IOException, TimeoutException {
        // TODO: wait for connection, when  work is done,
        // or when someone found the result, we propagate them

        // 4 types of requests
        // "NEW" : send a stack with one string: the hash
        // "WORK" : send a stack with work to do
        // "RESULT::"  : propagate result, stop wating

        // We send :
        // null : no more work to do

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(REQUEST_QUEUE_NAME, false, false, false, null);
        System.out.println(" [*] Waiting for requests. To exit press CTRL+C");

        // final Message[] msgObj_reply = new Message[1];

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                Message msgObj = Message.fromBytes(body);
                System.out.println(" [x] Received '" + msgObj.getMsg() + "'");
                try{
                    sendWork(msgObj.getMsg());
                } catch (Exception e) {
                    e.printStackTrace();
                } 
                // msgObj_reply[0] = Message.fromBytes(body);
                // System.out.println(" [x] Received '" + msgObj_reply[0].getMsg() + "'");
            }
        };
        channel.basicConsume(REQUEST_QUEUE_NAME, true, consumer);
    }

    public static void propagateResults(){
        // TODO: we send to the other servers the result of our clients
        // is it our job, or the LB job ?
    }

    public static void splitDictionnary(){
        Stack<String> stack = myPartition.getDict();
        Stack<String> smallerChunk = new Stack<>();


        while (!stack.empty()){
            if (smallerChunk.size() < chunkSize){
                smallerChunk.push(stack.pop());
            } else {
                chunks.push(smallerChunk);
                smallerChunk = new Stack<>();
                smallerChunk.push(stack.pop());
            }
        }

        if (!smallerChunk.empty()){
            chunks.push(smallerChunk);
        }
    }

    //EXPLAIN: send the work
    private static int sendWork(String clientID) throws Exception{
        System.out.println("Testing... Currently I have " + chunks.size() + " chunks");

        Connection connection;
        Channel channel;

        //EXPLAIN: The connection to send the work to a client is identify by SEND_WORK_QUEUE_NAME (for example: if send to "client1" the queue name is "work_queue_client1"
        String SEND_WORK_QUEUE_NAME = "work_queue" + clientID;
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        connection = factory.newConnection();
        channel = connection.createChannel();
        channel.queueDeclare(SEND_WORK_QUEUE_NAME, false, false, false, null);

        //EXPLAIN: the random is not important, just an example
        // Random rand = new Random();
        // int  n = rand.nextInt(50) + 1;
        // Message msgObj = new Message("Here is your work " + myPartition.getMsg());
        Dictionary dictObj = new Dictionary(chunks.pop(), myPartition.getInputHash());

        //EXPLAIN: Publish the work to the queue
        channel.basicPublish("", SEND_WORK_QUEUE_NAME, null, dictObj.toBytes());
        System.out.println(" [x] Sent to client the work");
        channel.close();
        connection.close();
        return 0;
    }
}
