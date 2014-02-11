package websocket;

import hazelcast.ClientInstance;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import model.StockRecord;
import model.StockResponse;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import serializer.ResponseSerializer;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;

@ServerEndpoint(value = "/websocket/stock")
public class StockWebSocket implements EntryListener<String, StockRecord> {

	private static final Log LOG = LogFactory.getLog(StockWebSocket.class);

	private static final Set<StockWebSocket> connections = new CopyOnWriteArraySet<StockWebSocket>();

	private Session session;

	private String listenerId = null;

	public StockWebSocket() {
		listenerId = ClientInstance.getInstance().getMap().addEntryListener(this, true);
	}

	@OnOpen
	public void start(Session session) {
		this.session = session;
		connections.add(this);
	}

	@OnClose
	public void end() {
		if (listenerId != null) {
			ClientInstance.getInstance().getMap().removeEntryListener(listenerId);
		}
		connections.remove(this);
	}

	@OnMessage
	public void incoming(String message) {
		if (message.equals("open")) {
			Set<String> keys = ClientInstance.getInstance().getMap().keySet();
			StockResponse response = new StockResponse();
			response.setStocks(keys);
			send(this, ResponseSerializer.getInstance().serialize(response));
		}
	}

	@OnError
	public void onError(Throwable t) throws Throwable {
	}

	private static void send(StockWebSocket client, String message) {
		try {
			synchronized (client) {
				client.session.getBasicRemote().sendText(message);
			}
		} catch (IOException e) {
			LOG.debug("Failed to send message to client", e);
			connections.remove(client);
			try {
				client.session.close();
			} catch (IOException ioException) {

			}
		}
	}

	@Override
	public void entryAdded(EntryEvent<String, StockRecord> event) {
	}

	@Override
	public void entryRemoved(EntryEvent<String, StockRecord> event) {
	}

	@Override
	public void entryUpdated(EntryEvent<String, StockRecord> event) {
		send(this, ResponseSerializer.getInstance().serialize(event.getValue()));
	}

	@Override
	public void entryEvicted(EntryEvent<String, StockRecord> event) {
	}
}
