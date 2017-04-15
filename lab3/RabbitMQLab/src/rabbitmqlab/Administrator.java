package rabbitmqlab;

import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Administrator {

    public static void main(String[] argv) throws Exception {

        // info
        System.out.println("ADMINISTRATOR");

        // connection & channel
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel admin_channel = connection.createChannel();
        Channel log_channel = connection.createChannel();

        // queue
        String admin_queue = "admin_queue";
        admin_channel.queueDeclare(admin_queue, false, false, false, null);

        String log_exchange = "log";
        log_channel.exchangeDeclare(log_exchange, BuiltinExchangeType.FANOUT);

        // consumer (handle msg)
        Consumer consumer = new DefaultConsumer(admin_channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received: " + message);
            }
        };


        // start listening
        System.out.println("Waiting for messages...");
        admin_channel.basicConsume(admin_queue, false, consumer);


        while (true) {

            // read msg
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Enter message: ");
            String message = br.readLine();

            // break condition
            if ("exit".equals(message)) {
                break;
            }

            // publish
            log_channel.basicPublish(log_exchange, "", null, message.getBytes("UTF-8"));
            System.out.println("Sent: " + message);
        }



        // close
        //channel.close();
        //connection.close();
    }
}
