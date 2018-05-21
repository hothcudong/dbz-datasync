import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shippo.sync.entities.database.v0.Customers;

/**
 * Created by copper on 08/05/2018.
 */
public class EbeanTest {
    Logger LOG = LoggerFactory.getLogger(EbeanTest.class);
    @Test
    public void ebeanTest(){
        EbeanServer dbsource = Ebean.getServer("inventory");
        Assert.assertTrue(dbsource.find(Customers.class).where().findList() != null);

    }

}
