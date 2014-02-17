package uk.co.c2b2.demo.hazelcast.undertow;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import model.StockRecord;
import serializer.ResponseSerializer;

import java.util.List;

class ChangedEntryListener implements EntryListener<String, StockRecord> {
    private final List<WebSocketChannel> sessions;
    public ChangedEntryListener(List<WebSocketChannel> sessions) {
        this.sessions = sessions;
    }

    public void entryAdded(EntryEvent<String, StockRecord> event) { }
    public void entryRemoved(EntryEvent<String, StockRecord> event) { }
    public void entryEvicted(EntryEvent<String, StockRecord> event) { }
    public void entryUpdated(EntryEvent<String, StockRecord> event) {
        for (final WebSocketChannel session : sessions) {
            WebSockets.sendText(ResponseSerializer.getInstance().serialize(event.getValue()), session, new io.undertow.websockets.core.WebSocketCallback<Void>() {
                public void complete(WebSocketChannel channel, Void context) {
                }

                public void onError(WebSocketChannel channel, Void context, Throwable throwable) {
                    sessions.remove(session);
                }
            });
        }
    }
}