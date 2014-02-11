package test;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.ExecutionException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import test.utilities.ClientUtility;
import test.utilities.ClusterUtility;

import com.hazelcast.core.Cluster;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.HazelcastInstance;

public class SimpleEntryListenerTest {

	private static ClusterUtility clusterUtility;
	private static int initialClusterSize = 3;
	private static int datasetSize = 100;
	private static int backupCount = 1;
	private static String mapName = "test-dataset";
	
	@BeforeClass
	public static void setupCluster() {
		clusterUtility = new ClusterUtility(initialClusterSize);
		clusterUtility.populateClusterDataset(mapName, datasetSize, backupCount);
	}

	@AfterClass
	public static void teardownCluster() {
		clusterUtility.shutdown();
	}

	@Test
	public void testOwnedEntryCount() throws InterruptedException, ExecutionException {

		HazelcastInstance client = ClientUtility.createClient();
        
        Cluster cluster = client.getCluster();
        assertTrue(cluster.getMembers().size() == initialClusterSize);
        
        String entryListenerUUID = client.getMap(mapName).addEntryListener(new MyEntryListener(), new Integer(1), true);
        System.out.println("entryListenerUUID: " + entryListenerUUID);
        
        Integer key = new Integer(1);
        
        client.getMap(mapName).set(key, "test");
        
        client.shutdown();

	}

}
class MyEntryListener implements EntryListener<Object, Object> {

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
