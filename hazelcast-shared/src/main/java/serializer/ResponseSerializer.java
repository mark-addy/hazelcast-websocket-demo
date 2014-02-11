package serializer;

import model.StockRecord;
import model.StockResponse;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.ObjectMapper;

public class ResponseSerializer {

	private final ObjectMapper mapper;

	private ResponseSerializer() {
		JsonFactory factory = new JsonFactory();
		mapper = new ObjectMapper(factory);
	}

	private static class ResponseSerializerHolder {
		private static final ResponseSerializer INSTANCE = new ResponseSerializer();
	}

	public static ResponseSerializer getInstance() {
		return ResponseSerializerHolder.INSTANCE;
	}

	public String serialize(Object response) {
		try {
			String responseString = mapper.writeValueAsString(response);
			System.out.println(responseString);
			return responseString;
		} catch (Exception e) {
			System.out.println("Unable to serialize Object: [" + response + "]");
			return null;
		}
	}

	public Object deserialize(String message) {
		try {
			Object record = mapper.readValue(message, StockRecord.class);
			return record;
		} catch (Exception exception) {
			System.out.println("Unable to deserialize String : [" + message + "]");
			try {
				Object record = mapper.readValue(message, StockResponse.class);
				return record;
			} catch (Exception e) {
				System.out.println("Unable to deserialize String : [" + message + "]");
				return null;
			}
		}
	}

}
