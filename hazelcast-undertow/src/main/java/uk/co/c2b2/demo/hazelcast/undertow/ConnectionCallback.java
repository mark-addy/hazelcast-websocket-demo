package uk.co.c2b2.demo.hazelcast.undertow;

import com.hazelcast.core.IMap;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.core.*;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import model.StockRecord;
import model.StockResponse;
import org.xnio.ChannelListener;
import serializer.ResponseSerializer;

import java.nio.channels.Channel;
import java.util.List;

class ConnectionCallback implements WebSocketConnectionCallback {
    private final IMap<String,StockRecord> map;
    private final List<WebSocketChannel> sessions;

    public ConnectionCallback(IMap<String,StockRecord> map, List<WebSocketChannel> sessions) {
        this.map = map;
        this.sessions = sessions;
    }

    @Override
    public void onConnect(WebSocketHttpExchange exchange, WebSocketChannel channel) {
        channel.getReceiveSetter().set(new AbstractReceiveListener() {
            @Override
            protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) {
                if ("open".equals(message.getData())) {
                    StockResponse response = new StockResponse();
                    response.setStocks(map.keySet()); //this is quite expensive operation as it hits all cluster members!
                    WebSockets.sendText(ResponseSerializer.getInstance().serialize(response), channel, new WebSocketCallback<Void>() {
                        @Override
                        public void complete(WebSocketChannel channel, Void context) {
                            sessions.add(channel);
                        }

                        @Override
                        public void onError(WebSocketChannel channel, Void context, Throwable throwable) {

                        }
                    });
                }
            }
        });
        channel.getCloseSetter().set(new ChannelListener<Channel>() {
            @Override
            public void handleEvent(Channel channel) {
                sessions.remove(channel);
            }
        });
        channel.resumeReceives();
    }
}