package test;

import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import model.StockRecord;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import test.utilities.ClientUtility;
import test.utilities.ClusterUtility;

import com.hazelcast.core.Cluster;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class StockMapEntryListenerTest {

	private static ClusterUtility clusterUtility;
	private static int initialClusterSize = 3;
	private static int backupCount = 1;
	private static String mapName = "stock-map";
	
	@BeforeClass
	public static void setupCluster() {
		clusterUtility = new ClusterUtility(initialClusterSize);
		clusterUtility.populateStockDataset(mapName, backupCount);
	}

	@AfterClass
	public static void teardownCluster() {
		clusterUtility.shutdown();
	}

	@Test
	public void testOwnedEntryCount() throws InterruptedException, ExecutionException {

		HazelcastInstance client = ClientUtility.createClient();
		
		StockUpdater stockUpdater = new StockUpdater(mapName);
		Thread stockUpdaterThread = new Thread(stockUpdater);
		stockUpdaterThread.start();
        
        Cluster cluster = client.getCluster();
        assertTrue(cluster.getMembers().size() == initialClusterSize);
        
//        String entryListenerUUID = client.getMap(mapName).addEntryListener(new StockEntryListener(), "TPK", true);
//        String entryListenerUUID = client.getMap(mapName).addEntryListener(new StockEntryListener(), true);
//        System.out.println("entryListenerUUID: " + entryListenerUUID);

		try {
			Thread.currentThread().sleep(30000000L);
		} catch (InterruptedException exception) {
			
		}
		stockUpdater.setStopFlag(true);
      
        client.shutdown();

	}

}
class StockEntryListener implements EntryListener<Object, Object> {

	@Override
	public void entryAdded(EntryEvent<Object, Object> event) {
		displayEvent(event);
	}

	@Override
	public void entryRemoved(EntryEvent<Object, Object> event) {
		displayEvent(event);
	}

	@Override
	public void entryUpdated(EntryEvent<Object, Object> event) {
		displayEvent(event);
	}

	@Override
	public void entryEvicted(EntryEvent<Object, Object> event) {
		displayEvent(event);
	}

	private void displayEvent(EntryEvent<Object, Object> event) {
		System.out.println(event.getEventType().toString() + " " + event.getKey() + " " + event.getValue());
	}
}

class StockUpdater implements Runnable {
	
	private Random random = new Random();
	
	private IMap<String, StockRecord> stockMap;

	private Double maximumMovement = 0.5D;
	
	private boolean stopFlag = false;
	
	StockUpdater(String mapName) {
		HazelcastInstance client = ClientUtility.createClient();
		stockMap = client.getMap(mapName);
	}
	
	@Override
	public void run() {

		while (!stopFlag) {

			int stockPositionToUpdate = random.nextInt(ClusterUtility.stockMap.size());
			StockRecord stockRecordToUpdate = stockMap.get(stockMap.keySet().toArray()[stockPositionToUpdate]);

			System.out.println("Updating " + stockRecordToUpdate.getSymbol());

			
			BigDecimal currentValue = stockRecordToUpdate.getValue();
			BigDecimal change = new BigDecimal(random.nextDouble() * maximumMovement);
			boolean positiveDirectionOfChange = random.nextBoolean();
			
			if (!positiveDirectionOfChange && wouldResultInNegativeValue(currentValue, change)) {
				positiveDirectionOfChange = !positiveDirectionOfChange;
			}
			
			if (positiveDirectionOfChange) {
				stockRecordToUpdate.setValue(increasePrice(currentValue, change));
			} else {
				stockRecordToUpdate.setValue(reducePrice(currentValue, change));
			}
			stockRecordToUpdate.setLastUpdate(new Date());

			stockMap.put(stockRecordToUpdate.getSymbol(), stockRecordToUpdate);
			
			try {
				Thread.currentThread().sleep(500L);
			} catch (InterruptedException exception) {
				
			}
		}
		
		System.out.println("Stopping updater thread");
		
	}

	public void setStopFlag(boolean stop) {
		this.stopFlag = stop;
	}
	
	private boolean wouldResultInNegativeValue(BigDecimal currentValue, BigDecimal change) {
		BigDecimal result = reducePrice(currentValue, change);
		if (result.compareTo(BigDecimal.ZERO) <= 0) {
			return true;
		}
		return false;
	}
	
	private BigDecimal reducePrice(BigDecimal currentValue, BigDecimal change) {
		return currentValue.subtract(change).setScale(2, BigDecimal.ROUND_HALF_UP);
	}
	private BigDecimal increasePrice(BigDecimal currentValue, BigDecimal change) {
		return currentValue.add(change).setScale(2, BigDecimal.ROUND_HALF_UP);
	}
}
