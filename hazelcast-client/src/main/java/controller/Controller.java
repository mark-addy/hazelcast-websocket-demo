package controller;

import java.io.IOException;

import javax.websocket.DeploymentException;

import org.jfree.ui.RefineryUtilities;

import chart.StockClient;
import client.StockWebSocketClient;

public class Controller {

	public static void main(String[] args) throws DeploymentException, IOException {
        final StockClient demo = new StockClient("Stock Price Web Socket Demo");
		StockWebSocketClient client = new StockWebSocketClient(demo);
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);
	}
}
