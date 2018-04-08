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

    //constructor: connecto the loadbalancer (rabbitmq)
    public Client() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        connection = factory.newConnection();
        channel = connection.createChannel();

        replyQueueName = channel.queueDeclare().getQueue();
    }

    //send the request
    public String call(String message) throws IOException, InterruptedException {
        final String corrId = UUID.randomUUID().toString();

        AMQP.BasicProperties props = new AMQP.BasicProperties
        .Builder()
        .correlationId(corrId)
        .replyTo(replyQueueName)
        .build();

        channel.basicPublish("", requestQueueName, props, message.getBytes("UTF-8"));

        final BlockingQueue<String> response = new ArrayBlockingQueue<String>(1);

        channel.basicConsume(replyQueueName, true, new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                if (properties.getCorrelationId().equals(corrId)) {
                    response.offer(new String(body, "UTF-8"));
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

        if (args.length < 1){
            System.out.println("This need an IP to connect to.");
        }

        Client workRequester = null;
        String response = null;
        try {
            workRequester = new Client();

            // get work
            System.out.println(" [x] Requesting for work");
            // 42 is just an example, you can send anything, number, string,etc.
            response = workRequester.call("42");
            System.out.println(" [.] Got '" + response + "'");

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
    }

    private static void sendResults(){
        // TODO: send result to out server
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
