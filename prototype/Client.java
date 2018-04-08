import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

public class Client {
    private static boolean workToDo;
    private static boolean resultFound;

    private Connection connection;
    private Channel channel;
    private String requestQueueName = "rpc_queue";
    private String replyQueueName;
    static Client workRequester = null;

    //constructor: connecto the loadbalancer (rabbitmq)
    public Client() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        connection = factory.newConnection();
        channel = connection.createChannel();

        replyQueueName = channel.queueDeclare().getQueue();
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

    //close the connection
    public void close() throws IOException {
        connection.close();
    }

    public static void main (String[] args){
        // TODO: connect to load balancer
        // get work
        // do the work
        // send results
        // quit once no more work or result found

        // if (args.length < 1){
        //     System.out.println("This need an IP to connect to.");
        // }

        // String response = null;
        Message msgObj;
        try {
            workRequester = new Client();

            // get work
            System.out.println(" [x] Requesting for work");
            // 42 is just an example, you can send anything, number, string,etc.
            msgObj = workRequester.call("0100002345");

            // System.out.println(" [.] Got '" + response + "'");
            System.out.println(" [.] Got '" + msgObj.getMsg() + "'");

            //do the work
            doWork(); 

            //send result
            sendResults();

        } catch (IOException | TimeoutException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (workRequester != null) {
                try {
                    workRequester.close();
                } catch (IOException _ignore) {}
            }
        }

        // connectToLoadBalancer();
        // while(workToDo && !resultFound){
        //     getWork();
        //     sendResults();
        // }
    }

    private static void doWork(){
        // TODO: do the work
        System.out.println(" [.] Pretending to work");
    }

    private static void sendResults(){
        // TODO: send result to out server
        String result = "Found! Its 42!";
        try {
            System.out.println(" [x] Send the result");
            //EXPLAIN: Notify the server about the result
            workRequester.call(result);
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

    private static void connectToLoadBalancer(){
        // TODO: connect to the load balancer
        resultFound = false;
        workToDo = true;
    }

    private static void getWork(){
        // TODO: request work from server
    }
}
