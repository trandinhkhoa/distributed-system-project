import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;
import java.time.LocalDateTime;

public class Client {
    static Connection connection;
    static Channel channel;
    private static String REQUEST_QUEUE_NAME = "request_queue";

    //constructor: connecto the loadbalancer (rabbitmq)
    public Client() throws IOException, TimeoutException {
    }

    public static void main (String[] args) throws Exception{
        // TODO: connect to load balancer
        // get work
        // do the work
        // send results
        // quit once no more work or result found

        // if (args.length < 1){
        //     System.out.println("This need an IP to connect to.");
        // }

        Message msgObj;
        try {
            getWork();
        } catch (IOException | TimeoutException | InterruptedException e) {
            e.printStackTrace();
        } finally {
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
                Message msgObj_reply = Message.fromBytes(body);
                System.out.println(" [x] Received '" + msgObj_reply.getMsg() + "'");
                // msgObj_reply[0] = Message.fromBytes(body);
                // System.out.println(" [x] Received '" + msgObj_reply[0].getMsg() + "'");
                try{
                    channel.close();
                    connection.close();
                } catch (Exception e) {
                    e.printStackTrace();
                } 
                //
                //EXPLAIN: Do work 
                doWork(); 
            }
        };
        channel.basicConsume(RECV_WORK_QUEUE_NAME, true, consumer);
    }
}
