package shippo.sync.kafka;

import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by copper on 07/05/2018.
 */
public abstract class KafkaConsumer {

    private Logger LOG = LoggerFactory.getLogger(KafkaConsumer.class);
    private String kafkaTopic;
    private String groupId;
    private String zookeeper;
    private int nOThread;
    private ConsumerConnector consumerConnector;
    private ExecutorService executorService;

    private AtomicInteger counter = new AtomicInteger(0);

    public KafkaConsumer(String zookeeper, String topic, String groupId, int nOThread){
        consumerConnector = kafka.consumer.Consumer.createJavaConsumerConnector(createConsumerConfig(zookeeper,groupId));
        this.kafkaTopic = topic;
        this.nOThread = nOThread;
        this.groupId = groupId;
        this.zookeeper = zookeeper;
        LOG.info("Start connect to zk: {}, topic: {}, groupId: {}, nOThreads: {}",zookeeper, topic, groupId, nOThread);
        run();
    }

    public void run(){

        Map<String, Integer> topicMap = new HashMap();
        topicMap.put(kafkaTopic, nOThread);

        Map<String, List<KafkaStream<byte[],byte[]>>> consumerMap = consumerConnector.createMessageStreams(topicMap);
        List<KafkaStream<byte[],byte[]>> kafkaStreams = consumerMap.get(kafkaTopic);

        executorService = Executors.newFixedThreadPool(nOThread);

        for(final KafkaStream stream: kafkaStreams){
            executorService.execute(new Runnable() {
                public void run() {
                    ConsumerIterator<byte[],byte[]> iterator =  stream.iterator();

                    while(iterator.hasNext()){
                       MessageAndMetadata<byte[],byte[]> data =  iterator.next();
                        try{
                            byte[] message = data.message();
                            processMessage(message);
                            consumerConnector.commitOffsets();
                            LOG.info("KafkaConsumer consumed the {}th message",counter.incrementAndGet());
                        }catch (Exception ex){
                            LOG.error("Cant consume message {}",ex);
                        }
                    }
                }
            });
        }
    }

    protected abstract void processMessage(byte[] data) throws Exception;

    private static ConsumerConfig createConsumerConfig(String zookeeper, String groupId) {

        Properties props = new Properties();
        props.put("zookeeper.connect", zookeeper);
        props.put("group.id", groupId);
        props.put("zookeeper.sync.time.ms", "200");
        // use this config to control commit for every message we recevive in active way
        props.put("enable.auto.commit", false);
        return new ConsumerConfig(props);
    }

    protected String getTopic(){
        return kafkaTopic;
    }
}
