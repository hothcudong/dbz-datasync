import com.avaje.ebean.EbeanServer;
import shippo.sync.entities.database.v0.Customers;
import shippo.sync.worker.SyncWorker;

public class DataSycTest {
    SyncWorker syncWorker;
    Customers customers;
    EbeanServer ebeanServer;
//    Random random = new Random();
//    @Before
//    public void datasyncInit(){
//        ebeanServer = Ebean.getServer("inventory");
//        customers = new Customers();
//        customers.setFirstName("test");
//        customers.setLastName("test");
//        customers.setEmail("test"+random.nextInt(1000)+"@gmail.com");
//
//
//
//    }
//    @Test
//    public void testGetEvent(){
//        ebeanServer.insert(customers);
//        new KafkaConsumer("localhost:2181","dbserver1.inventory.customers","test",1){
//            public void processMessage(byte[] msg){
//                Assert.assertTrue(msg != null);
//            }
//        };
//    }
}
