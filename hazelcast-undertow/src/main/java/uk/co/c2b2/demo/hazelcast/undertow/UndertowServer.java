package uk.co.c2b2.demo.hazelcast.undertow;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import io.undertow.Undertow;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.resource.ResourceManager;
import io.undertow.websockets.core.WebSocketChannel;
import model.StockRecord;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static io.undertow.Handlers.*;


public class UndertowServer {
    private static final String HOST = "localhost";
    private static final int PORT = 8080;
    private static final String WEBSOCKET_ENDPOINT_PATH = "websocket";

    private static final String HAZELCAST_CLUSTER_ADDRESS = "127.0.0.1:5701";
    private static final String STOCK_MAP_NAME = "stock-map";

    private final HazelcastInstance client;
    private final IMap<String,StockRecord> map;
    private final List<WebSocketChannel> sessions = new CopyOnWriteArrayList<WebSocketChannel>();

    private UndertowServer() {
        client = HazelcastClient.newHazelcastClient(new ClientConfig().addAddress(HAZELCAST_CLUSTER_ADDRESS));
        map = client.getMap(STOCK_MAP_NAME);
    }

    public static void main(String...ignored) {
        new UndertowServer().start();
    }

    public void start() {
        map.addEntryListener(new ChangedEntryListener(sessions), true);
        ResourceManager resourceManager = new ClassPathResourceManager( //FileResourceManager would be more effective (and secure?), but this allows to server static content from JAR
                UndertowServer.class.getClassLoader(), "public");

        Undertow.builder()
                .addHttpListener(PORT, HOST)
                .setHandler(
                        path(resource(resourceManager)) //serve static files by default
                                .addPrefixPath(WEBSOCKET_ENDPOINT_PATH, websocket(new ConnectionCallback(map, sessions)))
                        )
                .build()
                .start();
    }
}
