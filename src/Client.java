import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
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

public class Client {
    private static boolean workToDo;
    private static boolean resultFound;

    private static String inputHash;
    private static String result;

    private static Stack<String> work = new Stack<>();
    private static MessageDigest md;
    private final static char[] hexDigits = "0123456789abcdef".toCharArray();

    private String requestQueueName = "rpc_queue";
    private static Client workRequester = null;
    private static Connection connection = null;
    private static Channel channel = null;
    private static String replyQueueName;

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
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        connection = factory.newConnection();
        channel = connection.createChannel();

        replyQueueName = channel.queueDeclare().getQueue();

        resultFound = false;
        workToDo = true;

        try {
            inputHash = call("NEW").getMsg().pop();
        } catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    public static void main (String[] args){
        // TODO: connect to load balancer
        // get information
        // get work
        // do the work
        // send results
        // quit once no more work or result found

        if (args.length < 1){
            System.out.println("The client need an IP to connect to.");
            System.exit(0);
        }

        InetAddressValidator addressValidator = new InetAddressValidator();

        if (addressValidator.getInstance().isValidInet4Address(args[0]) == false){
            System.out.println("[Client] Please enter a proper IP address.");
            System.exit(1);
        }

        System.out.println("[Client] Connecting to " + args[0] + "...");

        // Get an message digest instance to compute a hash
        try {
            md = java.security.MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ex) {
            System.out.println("[Client] Unable to get an instance for that hashing algorithm.");
            System.exit(1);
        }

        // Connection to the load balancer is done via the constructor
        try {
            workRequester = new Client();
        } catch (IOException | TimeoutException  e){
            System.out.println("[Client] Unable to create client:");
            System.out.println(e.getStackTrace());
            System.exit(1);
        }

        while(workToDo && !resultFound){
            getWork();
            doWork();
            sendResults();
        }

        System.out.println("[Client] Done.");
    }

    private static void getWork(){
        // TODO: request work from server
        // If the server send NULL, we have no more work to do.
        // If the server send something, we add it to our chunk.
        // TODO: FIXME this is a placeholder for the real getWork()

        System.out.println("[Client] Requesting work");
        try {
            msgObj = workRequester.call("WORK");
        } catch (IOException | InterruptedException e){
            e.printStackTrace();
        }
        // The server send us a null object if there is no more work to do
        // or if the result has been found
        if (msgObj == null){
            System.out.println("[Client] No more work to do.");
            workToDo = false;
        }
    }

    private static void doWork(){
        System.out.println("[Client] Computing hashes...");
        while (!work.isEmpty() && !resultFound){
            String currentWord = work.pop();
            String currentHash = bytesToHex(md.digest(currentWord.getBytes()));
            if (currentHash.equals(inputHash)){
                result = currentWord;
                System.out.println("[Client] Password found ! We have \"" + currentWord + "\" which is " + currentHash + ".");
                resultFound = true;
            }
        }

    }

    //EXPLAIN: send the request, the return type should be the type of the object you want to send. In this example the return type is Message, an example class I defined, change it to your the class of your the object you wnat to send
    public Message call(String message) throws IOException, InterruptedException {
        final String corrId = UUID.randomUUID().toString();

        AMQP.BasicProperties props = new AMQP.BasicProperties
        .Builder()
        .correlationId(corrId)
        .replyTo(replyQueueName)
        .build();

        channel.basicPublish("", requestQueueName, props, message.getBytes("UTF-8"));

        //EXPLAIN: Create a queue with element of type Message (change this type to suit your need). 
        final BlockingQueue<Message> response = new ArrayBlockingQueue<Message>(1);

        channel.basicConsume(replyQueueName, true, new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                if (properties.getCorrelationId().equals(corrId)) {
                    //EXPLAIN: convert received byte array "body" to the type of the object you originally send. In this example byte[] is converted to Message using static method from Bytes
                    response.offer(Message.fromBytes(body));
                }
            }
        });

        return response.take();
    }

    private static void sendResults(){
        // send result to out server
        if (resultFound == true){
            try {
                System.out.println("[Client] Result being sent is: " + result);
                msgObj = workRequester.call("RESULT::" + result);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            } finally {
                if (workRequester != null) {
                    try {
                        workRequester.close();
                    } catch (IOException _ignore) {}
                }
            }
        }
    }

    //close the connection
    public void close() throws IOException {
        connection.close();
    }
}
