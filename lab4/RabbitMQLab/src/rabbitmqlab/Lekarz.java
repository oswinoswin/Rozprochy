package rabbitmqlab;

import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Lekarz {

    public static void main(String[] argv) throws Exception {

        // info
        System.out.println("LEKARZ");
        String EXCHANGE_NAME = "exchange";
        final String T_RESPONSE = "t_response";
        final String TASK = "task";


        // connection & channel
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        String elbow_queue = "elbow_queue";
        channel.queueDeclare(elbow_queue, false, false, false, null);

        channel.exchangeDeclare(T_RESPONSE, BuiltinExchangeType.FANOUT);
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
        channel.exchangeDeclare(TASK, BuiltinExchangeType.TOPIC);
        channel.basicQos(1);

        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, T_RESPONSE, "");
        System.out.println("created queue: " + queueName);


        // consumer (message handling)
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received: " + message);
            }
        };
        channel.basicConsume(queueName, true, consumer);



        while (true) {

            // read msg
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Enter message: ");
            String message = br.readLine();

            if(message.startsWith("ankle")){
                channel.basicPublish(TASK,"ankle", null, message.getBytes("UTF-8"));
            }else if(message.startsWith("elbow")){
                channel.basicPublish("", elbow_queue , null, message.getBytes());
            }else if(message.startsWith("knee")){
                channel.basicPublish(TASK, "knee", null, message.getBytes("UTF-8"));
            }



            // break condition
            if ("exit".equals(message)) {
                break;
            }

            // publish
            //channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes("UTF-8"));
            System.out.println("Sent: " + message);
        }



    }
}
