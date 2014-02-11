package chart;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import model.StockRecord;
import model.StockResponse;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;

public class StockClient extends ApplicationFrame {

	private static final long serialVersionUID = -3475528258968058069L;
	
	private Map<String, TimeSeries> timeSeriesMap = new HashMap<String, TimeSeries>();

	private final JPanel content;
	
	public StockClient(final String title) {
		super(title);
		content = new JPanel(new BorderLayout());
		setContentPane(content);
	}

	private JFreeChart createChart(final XYDataset dataset) {
		final JFreeChart result = ChartFactory.createTimeSeriesChart("Stock Prices", "Time", "Value", dataset, true, true, false);
		final XYPlot plot = result.getXYPlot();
		ValueAxis axis = plot.getDomainAxis();
		axis.setAutoRange(true);
		axis.setFixedAutoRange(60000.0); // 60 seconds
		axis = plot.getRangeAxis();
		axis.setRange(0.0, 15.0);
		return result;
	}

	public void updatePrice(StockRecord stockRecord) {
		timeSeriesMap.get(stockRecord.getSymbol()).addOrUpdate(new Millisecond(), stockRecord.getValue());
	}

	public void renderGraph(StockResponse stockResponse) {
		
		final TimeSeriesCollection dataset = new TimeSeriesCollection();
		for (String symbol : stockResponse.getStocks()) {
			TimeSeries timeSeries = new TimeSeries(symbol, Millisecond.class);
			timeSeriesMap.put(symbol, timeSeries);
			dataset.addSeries(timeSeries);
		}

		JFreeChart chart = createChart(dataset);
		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		content.add(chartPanel);
        this.pack();

	}

}