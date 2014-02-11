package test;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
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
	public void startCluster() throws InterruptedException, ExecutionException, IOException {

		HazelcastInstance client = ClientUtility.createClient();
		
		StockUpdater stockUpdater = new StockUpdater(mapName);
		Thread stockUpdaterThread = new Thread(stockUpdater);
		stockUpdaterThread.start();
        
        Cluster cluster = client.getCluster();
        assertTrue(cluster.getMembers().size() == initialClusterSize);

        System.in.read();

		stockUpdater.setStopFlag(true);
      
        client.shutdown();

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
