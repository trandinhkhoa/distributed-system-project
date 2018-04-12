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

import org.apache.commons.validator.routines.InetAddressValidator;

import java.time.LocalDateTime;
import java.util.Random;

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

    /**
     * Convert an array of bytes into a string that can be compared.
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

    //constructor: connect to the loadbalancer (rabbitmq)
    public Client() throws IOException, TimeoutException {
        // resultFound = false;
        // workToDo = true;

        // try {
        //     inputHash = call("NEW").getMsg().pop();
        // } catch (InterruptedException e){
        //     e.printStackTrace();
        // }
    }

    public static void main (String[] args) throws Exception{
        // TODO: connect to load balancer
        // get information
        // get work
        // do the work
        // send results
        // quit once no more work or result found

        // if (args.length < 1){
        //     System.out.println("The client need an IP to connect to.");
        //     System.exit(0);
        // }
        //
        // InetAddressValidator addressValidator = new InetAddressValidator();
        //
        // if (addressValidator.getInstance().isValidInet4Address(args[0]) == false){
        //     System.out.println("[Client] Please enter a proper IP address.");
        //     System.exit(1);
        // }
        //
        // System.out.println("[Client] Connecting to " + args[0] + "...");
        //
        // // Get an message digest instance to compute a hash
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

        // while(workToDo && !resultFound){
        //     getWork();
        //     doWork();
        //     sendResults();
        // }
    }

    private static void getWork() throws Exception{
        // TODO: request work from server
        //EXPLAIN: Request the work through the common queue for everyone REQUEST_QUEUE_NAME
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        String clientID = LocalDateTime.now().toString();
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

        // final Message[] msgObj_reply = new Message[1];

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                //EXPLAIN: Handle the event when work arrive from the server
                //EXPLAIN: Extract the info
                // msgObj_reply[0] = Message.fromBytes(body);
                // System.out.println(" [x] Received '" + msgObj_reply[0].getMsg() + "'");

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
                doWork(dictObj_for_work); 
            }
        };
        channel.basicConsume(RECV_WORK_QUEUE_NAME, true, consumer);
    }

    private static void doWork(Dictionary dictObj_for_work){
        Stack<String> work = dictObj_for_work.getDict();
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
            }
        }
    }

    // private static void sendResults(){
    //     // send result to out server
    //     if (resultFound == true){
    //         try {
    //             System.out.println("[Client] Result being sent is: " + result);
    //             msgObj = workRequester.call("RESULT::" + result);
    //         } catch (IOException | InterruptedException e) {
    //             e.printStackTrace();
    //         } finally {
    //             if (workRequester != null) {
    //                 try {
    //                     workRequester.close();
    //                 } catch (IOException _ignore) {}
    //             }
    //         }
    //     }
    // }
}
