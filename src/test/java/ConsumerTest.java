import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shippo.sync.kafka.KafkaConsumer;

/**
 * Created by copper on 07/05/2018.
 */
public class ConsumerTest {
    static Logger LOG = LoggerFactory.getLogger(ConsumerTest.class);
    public static void main(String[] args) {
        String kafkaTopic = "dbserver1.inventory.customers";
        String groupId = "customer";
        String zookeeper = "shippolab:2181";
        int nOfThreads = 1;

        new KafkaConsumer(zookeeper, kafkaTopic, groupId, nOfThreads) {
            protected void processMessage(byte[] data) {
                LOG.info("Receive message: {}", new String(data));
            }
        };
    }
}
