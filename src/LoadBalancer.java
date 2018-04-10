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
    private static String dictionnaryFile;

    private static int bigChunkSize;
    private static int numberOfWords;
    private static int numberOfServers;

    private static Stack<Stack<String>> bigChunks = new Stack<>();

    private static ArrayList<String> serverList = new ArrayList<>();

    private static Connection connection = null;
    private static final String RPC_QUEUE_NAME = "rpc_queue";

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
        if (!stack.empty()){
            bigChunks.add(stack);
        }
    }

    private static void distributeDictionnary(){
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

        try {
            connection = factory.newConnection();
            final Channel channel = connection.createChannel();
            channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);
            channel.basicQos(1);
            System.out.println("[LB] Awaiting RPC requests from servers...");

            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                        .Builder()
                        .correlationId(properties.getCorrelationId())
                        .build();

                    String reply = new String(body, "UTF-8");
                    //EXPLAIN: If the client send a message with text "Found!" at the beginning, then dont sendi work, notify the result to the screen instead
                    if (reply.substring(0,6).equals("Found!"))
                    {
                        System.out.println("Eureka ! "+ reply);
                        channel.basicAck(envelope.getDeliveryTag(), false);
                        // RabbitMq consumer worker thread notifies the RPC server owner thread
                        synchronized (this) {
                            this.notify();
                        }
                    } else {
                        //EXPLAIN: Initialize the object (array, list, whatever) you want to send here
                        Stack<String> msgContent = new Stack<>();
                        msgContent.push("HeLLo BoI");
                        Message msgObj = new Message(msgContent);

                        //Give works to the connected client
                        try {
                            System.out.println(" [.] sendWork(" + msgObj.getMsg() + ")");

                        } catch (RuntimeException e) {
                            System.out.println(" [.] " + e.toString());
                        } finally {
                            //EXPLAIN: in this line, msgObj.toBypes() convert the object "msgObj" to bytes, and channel.basicPublish push msgObj.toBytes() to the queue
                            channel.basicPublish("", properties.getReplyTo(), replyProps, msgObj.toBytes());

                            channel.basicAck(envelope.getDeliveryTag(), false);
                            // RabbitMq consumer worker thread notifies the RPC server owner thread
                            synchronized (this) {
                                this.notify();
                            }
                        }

                    }
                }
            };
            channel.basicConsume(RPC_QUEUE_NAME, false, consumer);
            // Wait and be prepared to consume the message from RPC client.
            while (true) {
                synchronized (consumer) {
                    try {
                        consumer.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        } finally {
            if (connection != null)
                try {
                    connection.close();
                } catch (IOException _ignore) {}
        }

    }


    private static void waitForClients(){
        // TODO: wait for a client to connect, use a rabiitMQ queue ?
        System.out.println("[LB] Waiting for clients to connect...");
        System.out.println("[LB] TODO");
    }
}

