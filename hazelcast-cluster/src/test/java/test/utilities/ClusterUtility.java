package test.utilities;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

import model.StockRecord;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class ClusterUtility {
	
	private static final Random random = new Random();
	
	private final LinkedList<HazelcastInstance> instances = new LinkedList<HazelcastInstance>();
	
	private final Config config;
	
	public final static LinkedHashMap<String, StockRecord> stockMap = new LinkedHashMap<String, StockRecord>() {{
		put("C2B2", new StockRecord("C2B2", "C2B2 Consulting Limited", new BigDecimal(8.31D).setScale(2, RoundingMode.HALF_UP)));
		put("TPK", new StockRecord("TPK", "Travis Perkins Limited", new BigDecimal(7.62D).setScale(2, RoundingMode.HALF_UP)));
		put("ORL", new StockRecord("ORL", "Oracle", new BigDecimal(4.32D).setScale(2, RoundingMode.HALF_UP)));
		put("BPL", new StockRecord("BPL", "British Petroleum", new BigDecimal(3.45D).setScale(2, RoundingMode.HALF_UP)));
		put("IBM", new StockRecord("IBM", "IBM Computers", new BigDecimal(1.99D).setScale(2, RoundingMode.HALF_UP)));
		put("DEB", new StockRecord("DEB", "Debenhams", new BigDecimal(0.77D).setScale(2, RoundingMode.HALF_UP)));
		put("TCG", new StockRecord("TCG", "Thomson Cook Group", new BigDecimal(6.25D).setScale(2, RoundingMode.HALF_UP)));
		put("TYT", new StockRecord("TYT", "Toyota", new BigDecimal(1.99D).setScale(2, RoundingMode.HALF_UP)));
	}};
	
	public ClusterUtility(int clusterSize) {
        config = new Config();
		for (int i = 0 ; i < clusterSize ; i++) {
			addClusterMember();
		}
	}

	public void shutdown() {
		for (HazelcastInstance instance : instances) {
			shutdownInstance(instance);
		}
	}

	public void shutdownRandomInstance() {
		shutdownInstance(getRandomInstance());
	}

	public void shutdownInstance(int instanceToShutdown) {
		shutdownInstance(instances.get(instanceToShutdown));
	}

	private void shutdownInstance(HazelcastInstance instance) {
		instance.shutdown();
	}
	
	private HazelcastInstance getRandomInstance() {
		int randomInstanceIndex = random.nextInt(instances.size());
		return instances.get(randomInstanceIndex);
	}

	public void populateStockDataset(String mapName, int backupCount) {
        buildAndApplyMapConfig(mapName, backupCount);
        Map<String, StockRecord> map = getRandomInstance().getMap(mapName);
        for (Map.Entry<String, StockRecord> stockRecordEntry : stockMap.entrySet()) {
        	System.out.println("inserting " + stockRecordEntry.getKey() + " value : " + stockRecordEntry.getValue());
        	map.put(stockRecordEntry.getKey(), stockRecordEntry.getValue());
        }
	}

	private void buildAndApplyMapConfig(String mapName, int backupCount) {
        MapConfig mapConfig = new MapConfig();
        mapConfig.setName(mapName);
        mapConfig.setBackupCount(backupCount);
        for (HazelcastInstance instance : instances) {
        	instance.getConfig().addMapConfig(mapConfig);
        }
	}
	
	public void addClusterMember() {
        HazelcastInstance instance = Hazelcast.newHazelcastInstance(config);
        instances.add(instance);
	}

	public void removeLastClusterMember() {
		shutdownInstance(instances.pollLast());
	}
	public void removeFirstClusterMember() {
		shutdownInstance(instances.pollFirst());
	}

}
