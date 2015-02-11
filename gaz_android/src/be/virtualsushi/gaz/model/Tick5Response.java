package be.virtualsushi.gaz.model;

public class Tick5Response {

	public enum ResponseStatuses {

		OK,
		ERROR;

	}

	public ResponseStatuses status;
	public Tick[] tweets;

}
