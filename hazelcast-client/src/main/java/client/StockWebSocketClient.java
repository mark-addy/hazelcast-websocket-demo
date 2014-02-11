package client;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;

import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import model.StockRecord;
import model.StockResponse;
import serializer.ResponseSerializer;
import chart.StockClient;

@ClientEndpoint
public class StockWebSocketClient {
	
	private StockClient demo;
	
	private Session session;
	
	public StockWebSocketClient(StockClient demo) throws DeploymentException, IOException {
		this.demo = demo;
		WebSocketContainer container = ContainerProvider.getWebSocketContainer();
		String uri = "ws://localhost:8080/hazelcast-web/websocket/stock";
		container.connectToServer(this, URI.create(uri));
	}
	
	@OnOpen
	public void onOpen(Session session) {
		System.out.println("opened");
		this.session = session;
		send("open");
	}

	@OnError
	public void onError(Throwable t) {
		System.out.println("error " + t.getMessage());
		t.printStackTrace();
	}

	@OnClose
	public void onClose() {
		System.out.println("close");
	}
	
	@OnMessage
	public void onMessage(String message) {
		System.out.println("message : " + message);
		Object messageObject = ResponseSerializer.getInstance().deserialize(message);
		if (messageObject instanceof StockRecord) {
			demo.updatePrice((StockRecord)messageObject);
		} else if (messageObject instanceof StockResponse) {
			System.out.println("Stock Response received : " + Arrays.deepToString(((StockResponse)messageObject).getStocks().toArray()));
			demo.renderGraph((StockResponse)messageObject);
		}
	}

	private void send(String message) {
		try {
			synchronized (this) {
				session.getBasicRemote().sendText(message);
			}
		} catch (IOException e) {
			try {
				session.close();
			} catch (IOException ioException) {

			}
		}
	}

}
