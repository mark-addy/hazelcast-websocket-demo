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

public class StockMapEntryListenerTest2 {

	private static ClusterUtility clusterUtility;
	private static int initialClusterSize = 3;
	private static int backupCount = 1;
	private static String mapName = "stock-map";
	
	@Test
	public void testOwnedEntryCount() throws InterruptedException, ExecutionException {

		HazelcastInstance client = ClientUtility.createClient();
		
        String entryListenerUUID = client.getMap(mapName).addEntryListener(new StockEntryListener2(), true);
        System.out.println("entryListenerUUID: " + entryListenerUUID);

		try {
			Thread.currentThread().sleep(300000L);
		} catch (InterruptedException exception) {
			
		}
      
        client.shutdown();

	}

}
class StockEntryListener2 implements EntryListener<Object, Object> {

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
