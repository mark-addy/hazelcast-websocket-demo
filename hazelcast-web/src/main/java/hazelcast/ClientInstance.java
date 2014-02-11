package hazelcast;

import model.StockRecord;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class ClientInstance {
	
	private final HazelcastInstance client;
	private final IMap<String, StockRecord> stockMap;
	
	private ClientInstance() {
		ClientConfig clientConfig = new ClientConfig();
		clientConfig.addAddress("127.0.0.1:5701");
		client = HazelcastClient.newHazelcastClient(clientConfig);
		stockMap = client.getMap("stock-map");
	}

	private static class ClientInstanceHolder {
		private static final ClientInstance INSTANCE = new ClientInstance();
	}
 
	public static ClientInstance getInstance() {
		return ClientInstanceHolder.INSTANCE;
	}

	public HazelcastInstance getClient() {
		return client;
	}

	public IMap<String, StockRecord> getMap() {
		return stockMap;
	}

}

