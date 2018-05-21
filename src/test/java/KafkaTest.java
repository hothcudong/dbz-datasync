import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shippo.sync.kafka.KafkaConsumer;
import shippo.sync.kafka.KafkaProducer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


public class KafkaTest {
    Logger LOG = LoggerFactory.getLogger(KafkaTest.class);
    List<Integer> listMsg = new ArrayList<>();
    @Before
    public void init(){
        KafkaProducer producer = new KafkaProducer("test","localhost:19092");

        Random rd = new Random();
        for(int i = 0; i < 1000; i++){
            listMsg.add(rd.nextInt(1000));
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                int count = 0;
                while (count < listMsg.size()){
                    try {
                        Thread.sleep(200l);
                        String message = listMsg.get(count)+"";
                        producer.pushMsg(message.getBytes());
                        LOG.info("Producer sent {} counter {}",listMsg.get(count), count);
                        count++;
                    }catch (Exception ex){
                        LOG.error("Cant sent message {} ",ex);
                    }
                }
            }
        }).start();

    }
    @Test
   public void test(){

        new KafkaConsumer("localhost:22181","test","test",1){
            protected void processMessage(byte[] msg){
                int consumCount = 0;
                String msgReceive = new String(msg);
                int originalMsg = Integer.parseInt(msgReceive);
                Assert.assertTrue(originalMsg == listMsg.get(consumCount));
                LOG.info("Receive {} counter {}", msgReceive, consumCount);
                consumCount++;
                try {
                    Thread.sleep(2000l);
                }catch(InterruptedException ex){
                    LOG.error("Intterupted when received msg: {}",ex);
                }

            }
        };
    }
}
