import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import shippo.sync.entities.Customer;
import shippo.sync.entities.Customers;

import java.util.List;

/**
 * Created by copper on 08/05/2018.
 */
public class EbeanTest {
    public static void main(String[] args) {
        EbeanServer dbsource = Ebean.getServer("v1");
        List<Customers> customersList = dbsource.find(Customers.class).findList();
        System.out.println(customersList);
       /* Customers customer = new Customers();
        customer.setId(999);
        customer.setEmail("htddhbk93");
        customer.setFirstName("htd");
        customer.setLastName("thuc");*/
//        Customer customer = new Customer();
//        customer.setId(995);
//        customer.setFirst_name("John");
//        customer.setLast_name("Snow");
//        customer.setEmail("bastard@gmail.com");
//        customer.setVersion(1);
//        dbsource.insert(customer);
//        System.out.println(dbsource.find(Customers.class).findList());


    }
}
