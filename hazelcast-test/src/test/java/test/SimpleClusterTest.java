package test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import test.utilities.ClusterUtility;
import callable.MyCallable;
import cluster.HazelcastNode;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.Cluster;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.LifecycleEvent;
import com.hazelcast.core.LifecycleListener;
import com.hazelcast.core.LifecycleService;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;

public class SimpleClusterTest {

	private static ClusterUtility clusterUtility;
	private static int initialClusterSize = 5;
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
	public void testClusterSize() {

        ClientConfig clientConfig = new ClientConfig();
        clientConfig.addAddress("127.0.0.1:5701");
        HazelcastInstance client = HazelcastClient.newHazelcastClient(clientConfig);
        
        Cluster cluster = client.getCluster();
        assertTrue(cluster.getMembers().size() == initialClusterSize);
        
        clusterUtility.addClusterMember();
        
        cluster = client.getCluster();
        assertTrue(cluster.getMembers().size() == initialClusterSize + 1);
        assertTrue(client.getMap(mapName).size() == datasetSize);

        clusterUtility.removeLastClusterMember();
        
        cluster = client.getCluster();
        assertTrue(cluster.getMembers().size() == initialClusterSize);
        assertTrue(client.getMap(mapName).size() == datasetSize);
        
        client.shutdown();

	}
	
	@Test
	public void testClientFailover() {

        ClientConfig clientConfig = new ClientConfig();
        clientConfig.addAddress("127.0.0.1:5701");
        HazelcastInstance client = HazelcastClient.newHazelcastClient(clientConfig);
        
        LifecycleService lifecycleService = client.getLifecycleService();
        lifecycleService.addLifecycleListener(new LifecycleListener() {
			public void stateChanged(LifecycleEvent event) {
				if (event.getState() == LifecycleEvent.LifecycleState.CLIENT_DISCONNECTED) {
					System.out.println("Client Disconnected ============================================");
				} else if (event.getState() == LifecycleEvent.LifecycleState.CLIENT_CONNECTED) {
					System.out.println("Client Connected ==============================================");
				}
			}
		});
        
        client.getCluster().addMembershipListener(new MembershipListener() {
			public void memberRemoved(MembershipEvent membershipEvent) {
				System.out.println("member removed : " + membershipEvent.getMember().getInetSocketAddress());
			}
			public void memberAdded(MembershipEvent membershipEvent) {
				System.out.println("member added : " + membershipEvent.getMember().getInetSocketAddress());
			}
		});
        
        Cluster cluster = client.getCluster();
        assertTrue(cluster.getMembers().size() == initialClusterSize);
        assertTrue(client.getMap(mapName).size() == datasetSize);
        
        clusterUtility.removeFirstClusterMember();
        
        try {
        	System.out.println("Sleeping and waiting for client failover and membership change....");
			Thread.currentThread().sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

       	System.out.println("Cluster Size : " + cluster.getMembers().size());
        assertTrue(cluster.getMembers().size() == initialClusterSize - 1);
        assertTrue(client.getMap(mapName).size() == datasetSize);

        clusterUtility.addClusterMember();
        
        cluster = client.getCluster();
        assertTrue(cluster.getMembers().size() == initialClusterSize);
        assertTrue(client.getMap(mapName).size() == datasetSize);
        
        client.shutdown();

	}
	
}
