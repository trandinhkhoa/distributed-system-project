import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class HashServer {

    private static String hashString;
    private static String loadBalancerIp;

    private static final String RPC_QUEUE_NAME = "rpc_queue";


    private static int sendWork(int n) {
        return 0;
    }

    public static void main (String[] args){
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
            //EXPLAIN: more establishing connection 
            connection = factory.newConnection();
            final Channel channel = connection.createChannel();
            channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);
            channel.basicQos(1);
            System.out.println(" [x] Awaiting RPC requests");

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
                    } else
                    {
                        //EXPLAIN: Initialize the object (array, list, whatever) you want to send here
                        Message msgObj = new Message("HeLLo BoI");

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

        // waitForClients();
        // propagateResults();
    }

    public static void getDictionnaryPart(){
        // TODO: get dictionnary part from the load balancer
    }

    public static void waitForClients(){
        // TODO: wait for connection, when  work is done,
        // or when someone found the result, we propagate them
    }

    public static void propagateResults(){
        // TODO: we send to the other servers the result of our clients
    }
}
