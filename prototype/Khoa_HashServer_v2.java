import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.Random;

public class HashServer {

    private static String hashString;
    private static String loadBalancerIp;

    //EXPLAIN: declare queue REQUEST_QUEUE_NAME used to receive request 
    private final static String REQUEST_QUEUE_NAME = "request_queue";


    //EXPLAIN: send the work
    private static int sendWork(String clientID) throws Exception{
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
        Random rand = new Random();
        int  n = rand.nextInt(50) + 1;
        Message msgObj = new Message("Here is your work " + n);

        //EXPLAIN: Publish the work to the queue
        channel.basicPublish("", SEND_WORK_QUEUE_NAME, null, msgObj.toBytes());
        System.out.println(" [x] Sent to client the work");
        channel.close();
        connection.close();
        return 0;
    }

    public static void main (String[] args) throws Exception{
        // TODO: bootstrap
        // we get the dictionnary from the load balancer
        //
        // then we wait for incoming connections
        // servers communicate between themselves with rings

        // if (args.length < 1){
        //     System.out.println("A server need a MD5 hash, and a rabbitMQ IP to connect to.");
        //     System.exit(0);
        // }

        // hashString = args[0];
        // loadBalancerIp = args[1];

        System.out.println("[Server] Starting...");

        getDictionnaryPart();
        // TODO : how do the server can communicate with each others ?
        // a ring yes, but how do they know where it is ?

        //EXPLAIN: connect to the load balancer (rabbitmq)
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        Connection connection = null;
        try {
            waitForClients();
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        } finally {
            if (connection != null)
                try {
                    connection.close();
                } catch (IOException _ignore) {}
        }
    }

    public static void getDictionnaryPart(){
        // TODO: get dictionnary part from the load balancer
    }

    //EXPLAIN: Waiting for client to send request
    public static void waitForClients() throws Exception{
        // TODO: wait for connection, when  work is done,
        // or when someone found the result, we propagate them
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(REQUEST_QUEUE_NAME, false, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

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
    }
}
