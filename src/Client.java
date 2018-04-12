import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import java.util.Stack;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

public class Client {
    private static boolean workToDo;
    private static boolean resultFound = false;

    private static String inputHash;
    private static String result;

    private static Stack<String> work = new Stack<>();
    private static MessageDigest md;
    private final static char[] hexDigits = "0123456789abcdef".toCharArray();

    static Connection connection;
    static Channel channel;
    private static String REQUEST_QUEUE_NAME = "request_queue";

    private static Message msgObj;
    private static String clientID = LocalDateTime.now().toString();

    private static int numberOfServer = 0;

    
    /**
     * Convert an array of bytes into a string that can be compared.
     * @param bytes the bytes to convert
     * @return the bytes as a string
     */
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++ ) {
            int d = bytes[i] & 0xFF;
            hexChars[i * 2] = hexDigits[d >>> 4];
            hexChars[i * 2 + 1] = hexDigits[d & 0x0F];
        }
        return new String(hexChars);
    }

    public static void main (String[] args) throws Exception{
        try {
            md = java.security.MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ex) {
            System.out.println("[Client] Unable to get an instance for that hashing algorithm.");
            System.exit(1);
        }

        // Connection to the load balancer is done via the constructor
        try {
            getWork();
        } catch (IOException | TimeoutException | InterruptedException e) {
            e.printStackTrace();
        } finally {
        }

    }

    /**
     * Request work a part of the dictionnary from the server.
     * @throws Exception 
     */
    private static void getWork() throws Exception{
        //EXPLAIN: Request the work through the common queue for everyone REQUEST_QUEUE_NAME
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        connection = factory.newConnection();
        channel = connection.createChannel();
        channel.queueDeclare(REQUEST_QUEUE_NAME, false, false, false, null);
        Message msgObj = new Message(clientID);
        channel.basicPublish("", REQUEST_QUEUE_NAME, null, msgObj.toBytes());
        System.out.println(" [x] Requesting for work by " + msgObj.getMsg() );

        //close communication after sent the request
        channel.close();
        connection.close();

        //EXPLAIN: Open personal queue RECV_WORK_QUEUE_NAME to specific server to receive the work
        String RECV_WORK_QUEUE_NAME = "work_queue" + clientID;
        factory.setHost("localhost");
        connection = factory.newConnection();
        channel = connection.createChannel();

        channel.queueDeclare(RECV_WORK_QUEUE_NAME, false, false, false, null);
        System.out.println(" [*] Waiting for work. To exit press CTRL+C");

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                //EXPLAIN: Handle the event when work arrive from the server
                //EXPLAIN: Extract the info

                Dictionary dictObj_for_work = Dictionary.fromBytes(body);
                System.out.println(" [x] Received the work");

                try{
                    channel.close();
                    connection.close();
                } catch (Exception e) {
                    e.printStackTrace();
                } 
                //
                //EXPLAIN: Do work 
                if (!dictObj_for_work.getResultFound()){
                    try {
                        doWork(dictObj_for_work); 
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                    }
                }else {
                    System.out.println("Result already found. Stop request");
                    System.exit(0);
                }
            }
        };
        channel.basicConsume(RECV_WORK_QUEUE_NAME, true, consumer);
    }

    private static void doWork(Dictionary dictObj_for_work) throws Exception{
    /**
     * Compute the hash of every string in the dictionnary we obtained and compare it to the result
     * @param dictObj_for_work the dictionnary object to compute
     */
        Stack<String> work = dictObj_for_work.getDict();

        numberOfServer = dictObj_for_work.getNumberMax();
        inputHash = dictObj_for_work.getInputHash();
        System.out.println("[Client] Size of the work is " + work.size());

        while (!work.isEmpty() && !resultFound){
            String currentWord = work.pop();
            System.out.println("[Client] Computing hashes... " + currentWord);
            String currentHash = bytesToHex(md.digest(currentWord.getBytes()));
            if (currentHash.equals(inputHash)){
                result = currentWord;
                System.out.println("[Client] Password found ! We have \"" + currentWord + "\" which is " + currentHash + ".");
                resultFound = true;
                try {
                    sendResult(currentWord);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                }
            }
        }

        if (work.isEmpty() && !resultFound){
            getWork();
        }
    }

    private static void sendResult(String result) throws Exception{
        // send result to out server
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        // String clientID = LocalDateTime.now().toString();
        connection = factory.newConnection();
        channel = connection.createChannel();
        channel.queueDeclare(REQUEST_QUEUE_NAME, false, false, false, null);
        Message msgObj = new Message("[Found]" + result);
        for (int i = 0; i < numberOfServer; i++){
            channel.basicPublish("", REQUEST_QUEUE_NAME, null, msgObj.toBytes());
        }
        System.out.println(" [!] Sent result '" + result + "'");
        
        //close communication after sent the request 
        channel.close();
        connection.close();
    }
}
