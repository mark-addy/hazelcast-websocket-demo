package client;

import java.io.IOException;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;

public class ThinJavaHazelcastClientDistributedExecution {

	
    public static void main(String[] args) throws IOException {
    	
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.addAddress("127.0.0.1:5701");
        HazelcastInstance client = HazelcastClient.newHazelcastClient(clientConfig);
        
        
        client.getExecutorService("test");
        char action = 'Y';
        
        while (action != 'q' && action != 'Q') {
	        int input = System.in.read();
	        action = (char)input;
	        System.out.println(input);
        }
        
        client.shutdown();
    }
    
}
