package model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class StockRecord implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String symbol;
	
	private String description;
	
	private BigDecimal value;
	
	private Date lastUpdate;

	public StockRecord() { 
		/* for de-serialization */
	}

	public StockRecord(String symbol, String description, BigDecimal value) {
		this.symbol = symbol;
		this.description = description;
		this.value = value;
		this.lastUpdate = new Date();
	}

	public BigDecimal getValue() {
		return value;
	}

	public void setValue(BigDecimal value) {
		this.value = value;
	}

	public Date getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public String getSymbol() {
		return symbol;
	}

	public String getDescription() {
		return description;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		String separator = " - ";
		StringBuilder builder = new StringBuilder(symbol);
		builder.append(separator);
		builder.append(description);
		builder.append(separator);
		builder.append(value);
		builder.append(separator);
		builder.append(lastUpdate.toString());
		return builder.toString();
	}

}
