package shippo.sync.worker;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.google.gson.Gson;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shippo.sync.entities.Customer;
import shippo.sync.kafka.KafkaConsumer;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by copper on 07/05/2018.
 */
public class SyncWorker extends KafkaConsumer{

    private Logger LOG = LoggerFactory.getLogger(SyncWorker.class);
    private Gson gson = new Gson();
    private static Map<String, Integer> dbMap = new HashMap();
    private static Map<String, EbeanServer> datasourceMap = new HashMap();
    private String targetSource;
    private EbeanServer datasourceV0,datasourceV1;

    private static final char UPDATE = 'u';
    private static final char CREATE = 'c';
    private static final char DELETE = 'd';

    private String DATABASE_V0  = "inventory";
    private String DATABASE_V1  = "inventory_v1";

    public SyncWorker(String zookeeper, String topic, String groupId, int nOThread,String targetSource) {

        super(zookeeper, topic, groupId, nOThread);
        this.targetSource = targetSource;
        datasourceV0 = Ebean.getServer(DATABASE_V0);
        datasourceV1 = Ebean.getServer(DATABASE_V1);

        dbMap.put(DATABASE_V0, 0);
        dbMap.put(DATABASE_V1, 1);

        datasourceMap.put(DATABASE_V0, datasourceV0);
        datasourceMap.put(DATABASE_V1, datasourceV1);
    }

    protected void processMessage(byte[] data) throws Exception {
        // build json object
        JSONObject json = new JSONObject(new String(data));
        LOG.info("Kafka consumer for topic {}, received event {}", getTopic(), json.toString());

        if(json.isNull("payload")){
            return;
        }

        JSONObject payload = (JSONObject) json.get("payload");


        LOG.info("Object payload: {}",json.get("payload").toString());
        syncData(payload);
        // apply action tto entity object in wanted desired database
    }

    private void syncData(JSONObject payload){

        String dbName = getDBSource(payload);

        if(payload.isNull("op")) {
            LOG.info("Event has no operation");
            return;
        }
        String type = payload.getString("op");
        // check if (dupblicate) => not sync;

        switch (type.charAt(0)){
            case CREATE :{
                // insert into database
                insert(payload, dbName);
                break;
            }
            case DELETE :{
                //delete record in db
                delete(payload, dbName);
                break;
            }
            case UPDATE :{
                // update record use after result
                update(payload, dbName);
                break;
            }
            default:{
                LOG.info("Action {} hasnt been defined yet", type);
            }
        }
    }

    private void insert(JSONObject payload, String db){

        if(payload.isNull("after")){
            LOG.info("After element is null");
            return;
        }

        JSONObject after = (JSONObject) payload.get("after");
        Customer customers = gson.fromJson(after.toString(),Customer.class);

        // targetSource also original source
        if(dbMap.get(db) != customers.getVersion()){
            return;
        }
        // sync data to other source
        EbeanServer ebeanServer = datasourceMap.get(targetSource);
        ebeanServer.insert(customers);
        LOG.info("Sync customer {} to database {}, type {}", customers, targetSource, "INSERT");
    }

    private void update(JSONObject payload, String db){

        if(payload.isNull("after") || payload.isNull("before")){
            LOG.info("After or before element is null");
            return;
        }

        JSONObject after = (JSONObject) payload.get("after");
        Customer customers = gson.fromJson(after.toString(),Customer.class);

        if(dbMap.get(db) != customers.getVersion()){
            return;
        }
        EbeanServer ebeanServer = datasourceMap.get(targetSource);
        Customer sourceRecord = ebeanServer.find(Customer.class,customers.getId());
        cloneObj(sourceRecord, customers);
        ebeanServer.update(sourceRecord);
        LOG.info("Sync customer {} to database {}, type {}", customers, targetSource, "UPDATE");
    }

    public void cloneObj(Customer oldObj, Customer cloneObj){
        oldObj.setId(cloneObj.getId());
        oldObj.setFirst_name(cloneObj.getFirst_name());
        oldObj.setLast_name(cloneObj.getLast_name());
        oldObj.setEmail(cloneObj.getEmail());
        oldObj.setVersion(cloneObj.getVersion());
    }
    private void delete(JSONObject payload, String db){
        if(payload.isNull("before")) {
            return;
        }
        // delete customer from before element
        JSONObject before = (JSONObject) payload.get("before");
        Customer customers = gson.fromJson(before.toString(),Customer.class);

        if(dbMap.get(db) != customers.getVersion()){
            return;
        }

        EbeanServer ebeanServer = datasourceMap.get(targetSource);
        ebeanServer.delete(customers);

        LOG.info("Sync customer {} to database {}, type {}",customers, targetSource, "DELETE");
    }


    private String getDBSource(JSONObject payload){
        JSONObject source = (JSONObject) payload.get("source");
        String database = source.getString("db");
        LOG.info("Event was generated from database {}", database);
        return database;
    }


    public static void main(String[] args) {
        new SyncWorker("localhost:2181","dbserver1.inventory.customers","customer",1,"inventory_v1");
        new SyncWorker("localhost:2181","dbserver1.inventory_v1.customers","customer",1,"inventory");
    }
}
