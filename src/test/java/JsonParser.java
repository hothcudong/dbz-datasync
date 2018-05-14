import com.google.gson.Gson;
import org.json.JSONObject;
import shippo.sync.entities.Customers;

/**
 * Created by copper on 08/05/2018.
 */
public class JsonParser {
    public static void main(String[] args) {
        Gson gson = new Gson();
        String jsonString = "{\"op\":\"u\",\"before\":{\"last_name\":\"Kretchmar\",\"id\":1004,\"first_name\":\"Copper\",\"email\":\"annek@noanswer.org\"},\"after\":{\"last_name\":\"Kretchmar\",\"id\":1004,\"first_name\":\"Copper HO\",\"email\":\"annek@noanswer.org\"},\"source\":{\"ts_sec\":1525755219,\"file\":\"mysql-bin.000003\",\"pos\":721,\"name\":\"dbserver1\",\"gtid\":null,\"row\":0,\"thread\":2,\"server_id\":223344,\"version\":\"0.7.5\",\"snapshot\":false,\"db\":\"inventory\",\"table\":\"customers\"},\"ts_ms\":1525755219392}\n";
        JSONObject json = new JSONObject(jsonString);
        JSONObject after = (JSONObject) json.get("after");
        Customers customers = gson.fromJson(after.toString(),Customers.class);
        System.out.println(customers);

        JSONObject source = (JSONObject) json.get("source");
        String db = source.getString("db");
        System.out.println(db);

    }
}
