import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shippo.sync.entities.database.v0.Customers;
import shippo.sync.kafka.KafkaConsumer;
import shippo.sync.worker.SyncWorker;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by copper on 07/05/2018.
 */
public class DbzGetEvent {
    static Logger LOG = LoggerFactory.getLogger(DbzGetEvent.class);
    String kafkaTopic;
    String groupId;
    String zookeeper;
    int nOfThreads;
    Customers customer;
    EbeanServer ebeanServer;
    Random random;

    AtomicBoolean isEventArrived = new AtomicBoolean(false);
    AtomicBoolean isOperatorParsed = new AtomicBoolean(false);
    boolean isDone = false;

    @Before
    public void before(){
        random = new Random();
        kafkaTopic = "dbserver1.inventory.customers";
        groupId = "user";
        zookeeper = "localhost:22181";
        nOfThreads = 1;

        ebeanServer = Ebean.getServer("inventory");

        customer = new Customers();
        customer.setFirstName("copper");
        customer.setLastName("ho");
        customer.setEmail("copper"+random.nextInt(1000)+"@gmail.com");
        customer.setVersion(0);
        ebeanServer.insert(customer);

        LOG.info("Insert new record into customers : {}"
                ,ebeanServer.find(Customers.class).where().eq("email",customer.getEmail()).findUnique());
    }
    @Test
    public void consumerTest()  {

        new KafkaConsumer(zookeeper, kafkaTopic, groupId, nOfThreads) {
            protected void processMessage(byte[] data) {

                LOG.info("Receive event : {}", new String(data));

                if(data != null){
                    isEventArrived.set(true);
                }

                JSONObject event = new JSONObject(new String(data));
                LOG.info("Kafka consumer for topic {}, received event {}", getTopic(), event.toString());

                if(event.isNull("payload")){
                    return;
                }

                JSONObject payload = (JSONObject) event.get("payload");

                String type = payload.getString("op");
                char operation = type.charAt(0);
                if(operation == 'c'|| operation == 'd' || operation == 'u'){
                    isOperatorParsed.set(true);
                }
                LOG.info("Operation type: ",operation);

                ebeanServer.delete(customer);
                LOG.info("Ebean delete customer record: {}",customer);
                isDone = true;
            }
        };

        while (!isDone){
            try {
                Thread.sleep(200);
            }catch (Exception ex){
                LOG.error("Inter");
            }
        }

    }


}
