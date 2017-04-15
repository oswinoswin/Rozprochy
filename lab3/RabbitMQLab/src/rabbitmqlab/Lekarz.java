package rabbitmqlab;

import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Lekarz {

    public static void main(String[] argv) throws Exception {

        // info
        System.out.println("LEKARZ");



        // connection & channel
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();

        final Channel elbow_channel = connection.createChannel();
        final  Channel knee_channel = connection.createChannel();
        final  Channel ankle_channel = connection.createChannel();
        final  Channel response_channel = connection.createChannel();
        final  Channel admin_channel = connection.createChannel();
        final  Channel log_channel = connection.createChannel();

        String elbow_queue = "elbow_queue";
        String knee_queue = "knee_queue";
        String ankle_queue = "ankle_queue";
        String response_queue = "response_queue";
        String admin_queue = "admin_queue";

        elbow_channel.queueDeclare(elbow_queue, false, false, false, null);
        knee_channel.queueDeclare(knee_queue, false, false, false, null);
        ankle_channel.queueDeclare(ankle_queue, false, false, false, null);
        response_channel.queueDeclare(response_queue, false, false, false, null);
        admin_channel.queueDeclare(admin_queue, false, false, false, null);

        String log_exchange = "log";
        log_channel.exchangeDeclare(log_exchange, BuiltinExchangeType.FANOUT);
        // queue & bind
        String log_queue = log_channel.queueDeclare().getQueue();
        log_channel.queueBind(log_queue, log_exchange, "");
        System.out.println("created queue: " + log_queue);

        // consumer (message handling)
        Consumer log_consumer = new DefaultConsumer(log_channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received from admin: " + message);
            }
        };

        log_channel.basicConsume(log_queue, true, log_consumer);

        // consumer (handle msg)
        Consumer response_consumer = new DefaultConsumer(response_channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received: " + message);
            }
        };

        response_channel.basicConsume(response_queue, true, response_consumer);



        while (true) {

            // read msg
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Enter message: ");
            String message = br.readLine();

            if(message.startsWith("ankle")){
                ankle_channel.basicPublish("", ankle_queue, null, message.getBytes());
            }else if(message.startsWith("elbow")){
                elbow_channel.basicPublish("", elbow_queue, null, message.getBytes());
            }else if(message.startsWith("knee")){
                knee_channel.basicPublish("", knee_queue, null, message.getBytes());
            }

            admin_channel.basicPublish("", admin_queue, null,message.getBytes());



            // break condition
            if ("exit".equals(message)) {
                break;
            }
            System.out.println("Sent: " + message);
        }





    }
}
