package test.utilities;

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

	public void populateStockDataset(String mapName, LinkedHashMap<String, StockRecord> stockMap, int backupCount) {
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
