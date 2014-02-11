package callable;

import java.io.Serializable;
import java.util.concurrent.Callable;

public class MyCallable implements Callable<Long>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3893133189495426425L;
	
	int input = 0;

	public MyCallable() {
	}

	public MyCallable(int input) {
		this.input = input;
	}

	public Long call() {
		return calculate(input);
	}

	private long calculate(int n) {
		if (n <= 1)
			return n;
		else
			return calculate(n - 1) + calculate(n - 2);
	}
}