package test.utilities;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.LifecycleEvent;
import com.hazelcast.core.LifecycleListener;
import com.hazelcast.core.LifecycleService;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;

public class ClientUtility {

	public static HazelcastInstance createClient() {
		return createClient(true, true);
	}

	public static HazelcastInstance createClient(boolean addLifecycleListener,
			boolean addMembershipListener) {

		ClientConfig clientConfig = new ClientConfig();
		clientConfig.addAddress("127.0.0.1:5701");
		HazelcastInstance client = HazelcastClient
				.newHazelcastClient(clientConfig);

		if (addLifecycleListener) {
			addLifecycleListener(client);
		}

		if (addMembershipListener) {
			addMembershipListener(client);
		}
		
		return client;

	}

	private static void addLifecycleListener(HazelcastInstance client) {

		LifecycleService lifecycleService = client.getLifecycleService();
		lifecycleService.addLifecycleListener(new LifecycleListener() {
			public void stateChanged(LifecycleEvent event) {
				if (event.getState() == LifecycleEvent.LifecycleState.CLIENT_DISCONNECTED) {
					System.out
							.println("Client Disconnected ============================================");
				} else if (event.getState() == LifecycleEvent.LifecycleState.CLIENT_CONNECTED) {
					System.out
							.println("Client Connected ==============================================");
				} else {
					System.out.println(event.getState());
				}
			}
		});

	}

	private static void addMembershipListener(HazelcastInstance client) {

		client.getCluster().addMembershipListener(new MembershipListener() {
			public void memberRemoved(MembershipEvent membershipEvent) {
				System.out.println("member removed : "
						+ membershipEvent.getMember().getInetSocketAddress());
			}

			public void memberAdded(MembershipEvent membershipEvent) {
				System.out.println("member added : "
						+ membershipEvent.getMember().getInetSocketAddress());
			}
		});

	}
}
