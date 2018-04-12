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

public class HashServer {

    private static String hashString;
    private static String loadBalancerIp;

    //EXPLAIN: declare queue REQUEST_QUEUE_NAME used to receive request
    private final static String REQUEST_QUEUE_NAME = "request_queue";
    private final static String DISTRIBUTE_QUEUE_NAME = "distribute_queue";

    private static Dictionary myPartition = new Dictionary(0); //placeholder, this is the part of the dictionary the server receveive from the LB

    private static Stack<Stack<String>> chunks = new Stack<>();
    private static final Integer chunkSize = 100000;

    public static void main (String[] args){
        try {
            getDictionnaryPart();
        } catch (IOException | TimeoutException e){
            System.out.println("[Server] Error obtaining dictionnary part.");
            System.out.println(e.getMessage());
            System.exit(1);
        }

        //Wait and send work
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

    /**
     * Obtain a part of the dictionnary.
     * @throws IOException
     * @throws TimeoutException 
     */
    public static void getDictionnaryPart() throws IOException, TimeoutException{
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(DISTRIBUTE_QUEUE_NAME, false, false, false, null);
        System.out.println("[Server]  [*] Waiting for a Dictionary Partition. To exit press CTRL+C");

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                Dictionary dictObj = Dictionary.fromBytes(body);
                System.out.println("[Server]  [x] Received partition '" + dictObj.getNumber() + "'");
                try{
                    storePartition(dictObj);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        channel.basicConsume(DISTRIBUTE_QUEUE_NAME, true, consumer);
    }

    /**
     * Store the received partition of the dictionary.
     * @param partition the dictionary ot store.
     * @throws Exception 
     */
    public static void storePartition(Dictionary partition) throws Exception{
        myPartition = partition;
        hashString = myPartition.getInputHash();
        System.out.println("[Server]  [x] Saved my partition '" + myPartition.getNumber() + "'");

        splitDictionnary();
    }

    /**
     * Wait for clients to request works.
     * @throws IOException
     * @throws TimeoutException 
     */
    public static void waitForClients() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(REQUEST_QUEUE_NAME, false, false, false, null);
        System.out.println("[Server]  [*] Waiting for requests. To exit press CTRL+C");

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                Message msgObj = Message.fromBytes(body);
                if (msgObj.getMsg().substring(0,7).equals("[Found]")){
                    System.out.println("[Found] Original text of MD5 hash string '" + hashString + "' is '" + msgObj.getMsg().substring(7));
                    System.out.println("[New Session] Waiting for new request to inverse hash from LoadBalancer ... ");
                    chunks.clear();
                }else{
                    System.out.println("[Server]  [x] Received '" + msgObj.getMsg() + "'");
                    try{
                        sendWork(msgObj.getMsg());
                    } catch (Exception e) {
                        e.printStackTrace();
                    } 
                }
                // msgObj_reply[0] = Message.fromBytes(body);
                // System.out.println(" [x] Received '" + msgObj_reply[0].getMsg() + "'");
            }
        };
        channel.basicConsume(REQUEST_QUEUE_NAME, true, consumer);
    }

    /**
     * Send the results back to the other servers.
     */
    public static void propagateResults(){
        // TODO: we send to the other servers the result of our clients
        // is it our job, or the LB job ?
    }

    /**
     * Split the received dictionary into smaller chunks to be sent to the clients.
     */
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

    /**
     * Send a chunk to a client.
     * @param clientID the id of the client to send to.
     * @throws Exception 
     */
    private static void sendWork(String clientID) throws Exception{
        System.out.println("[Server] Testing... Currently I have " + chunks.size() + " chunks");

        Connection connection;
        Channel channel;
        Dictionary dictObj;

        //EXPLAIN: The connection to send the work to a client is identify by SEND_WORK_QUEUE_NAME (for example: if send to "client1" the queue name is "work_queue_client1"
        String SEND_WORK_QUEUE_NAME = "work_queue" + clientID;
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        connection = factory.newConnection();
        channel = connection.createChannel();
        channel.queueDeclare(SEND_WORK_QUEUE_NAME, false, false, false, null);

        if (!chunks.isEmpty()){
            dictObj = new Dictionary(chunks.pop(), myPartition.getInputHash(), myPartition.getNumber(), myPartition.getNumberMax());
            channel.basicPublish("", SEND_WORK_QUEUE_NAME, null, dictObj.toBytes());
            System.out.println("[Server]  [x] Sent to client the work");
        } else {
            System.out.println("[Server] No more part to send to clients.");
            System.exit(0);
        }

        //EXPLAIN: Publish the work to the queue
        channel.close();
        connection.close();
    }
}
