package test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import test.utilities.ClientUtility;
import test.utilities.ClusterUtility;
import callable.MyCallable;
import cluster.HazelcastNode;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.Cluster;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.LifecycleEvent;
import com.hazelcast.core.LifecycleListener;
import com.hazelcast.core.LifecycleService;
import com.hazelcast.core.Member;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;
import com.hazelcast.core.MultiExecutionCallback;
import com.hazelcast.monitor.LocalMapStats;

public class SimpleDistributedExectionTest {

	private static ClusterUtility clusterUtility;
	private static int initialClusterSize = 4;
	private static int datasetSize = 100;
	private static int backupCount = 2;
	private static String mapName = "test-dataset";
	
	@BeforeClass
	public static void setupCluster() {
		clusterUtility = new ClusterUtility(initialClusterSize);
		/* sleep required to ensure back-up copies are replicated completely */
		clusterUtility.populateClusterDataset(mapName, datasetSize, backupCount, 60000L);
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

        IExecutorService executorService = client.getExecutorService("test-exec-service"); 
        Map<Member, Future<Long>> futureResultMap = executorService.submitToAllMembers(new OwnedEntryCountCallable(mapName));
        int totalOwnedEntryCount = 0;
        for (Map.Entry<Member, Future<Long>> entry : futureResultMap.entrySet()) {
        	Long ownedEntryCount = entry.getValue().get();
        	System.out.println("Member : " + entry.getKey().getInetSocketAddress() + " reports " + ownedEntryCount);
        	totalOwnedEntryCount += ownedEntryCount;
        }
        
        assertTrue(totalOwnedEntryCount == datasetSize);
        
        client.shutdown();

	}
	
	@Test
	public void testLocalMapStats() throws InterruptedException, ExecutionException {

		HazelcastInstance client = ClientUtility.createClient();
        
        Cluster cluster = client.getCluster();
        assertTrue(cluster.getMembers().size() == initialClusterSize);

        IExecutorService executorService = client.getExecutorService("test-exec-service"); 
        Map<Member, Future<LocalMapStats>> futureResultMap = executorService.submitToAllMembers(new LocalMapStatsCallable(mapName));
        int totalOwnedEntryCount = 0;
        int totalBackupEntryCount = 0;
        for (Map.Entry<Member, Future<LocalMapStats>> entry : futureResultMap.entrySet()) {
        	LocalMapStats localMapStats = entry.getValue().get();
        	System.out.println(entry.getKey().getInetSocketAddress() + " ---- " + localMapStats);
        	totalOwnedEntryCount += localMapStats.getOwnedEntryCount();
        	totalBackupEntryCount += localMapStats.getBackupEntryCount();
        }
        
        assertTrue(totalOwnedEntryCount == datasetSize);
        assertTrue(totalBackupEntryCount == datasetSize * backupCount);
        
        client.shutdown();

	}

	@Test
	public void testLocalMapStatsWithCallback() throws InterruptedException, ExecutionException {

		HazelcastInstance client = ClientUtility.createClient();
        
        Cluster cluster = client.getCluster();
        assertTrue(cluster.getMembers().size() == initialClusterSize);

        IExecutorService executorService = client.getExecutorService("test-exec-service"); 

        final CountDownLatch doneLatch = new CountDownLatch(1);

        MyExecutionCallbackListener multiExecutionCallback = new MyExecutionCallbackListener(doneLatch);
        
		executorService.submitToAllMembers(new LocalMapStatsCallable(mapName), multiExecutionCallback);
        
        System.out.println("Wating for work to complete");
        doneLatch.await();
        System.out.println("Work has completed");

        assertTrue(multiExecutionCallback.getTotalOwnedEntryCount() == datasetSize);
        assertTrue(multiExecutionCallback.getTotalBackupEntryCount() == datasetSize * backupCount);

        client.shutdown();

	}

}

class OwnedEntryCountCallable implements Callable<Long>, HazelcastInstanceAware, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7435960693028170402L;

	private transient HazelcastInstance localInstance;
	
	private String mapName;
	
	public OwnedEntryCountCallable(String mapName) {
		this.mapName = mapName;
	}
	
	public Long call() throws Exception {
		return localInstance.getMap(mapName).getLocalMapStats().getOwnedEntryCount();
	}

	public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
		this.localInstance = hazelcastInstance;
	}
	
}

class LocalMapStatsCallable implements Callable<LocalMapStats>, HazelcastInstanceAware, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7435960693028170402L;

	private transient HazelcastInstance localInstance;
	
	private String mapName;
	
	public LocalMapStatsCallable(String mapName) {
		this.mapName = mapName;
	}
	
	public LocalMapStats call() throws Exception {
		return localInstance.getMap(mapName).getLocalMapStats();
	}

	public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
		this.localInstance = hazelcastInstance;
	}
	
}

class MyExecutionCallbackListener implements MultiExecutionCallback {

	private int totalOwnedEntryCount = 0;
	private int totalBackupEntryCount = 0;
	private final CountDownLatch doneLatch;
	
	public MyExecutionCallbackListener(CountDownLatch doneLatch) {
		this.doneLatch = doneLatch;
	}
	
	public int getTotalOwnedEntryCount() {
		return totalOwnedEntryCount;
	}

	public int getTotalBackupEntryCount() {
		return totalBackupEntryCount;
	}

	public void onResponse(Member member, Object value) {
		System.out.println(member.getInetSocketAddress() + " has returned response : " + value.toString());
	}
	
	public void onComplete(Map<Member, Object> values) {

        System.out.println("onComplete is called");
        
        
        for (Map.Entry<Member, Object> entry : values.entrySet()) {
        	
        	LocalMapStats localMapStats = (LocalMapStats)entry.getValue();
        	totalOwnedEntryCount += localMapStats.getOwnedEntryCount();
        	totalBackupEntryCount += localMapStats.getBackupEntryCount();
            
        }

        System.out.println("onComplete has finished, counting down");
        doneLatch.countDown();
        
	}

}
