package org.joshvm.ams.console;

public final class ConsoleCommand {
	private int type;
	private String message;
	public static final int NOTIFY_REMOTE_DISCONNECTED = 1;
	public static final int NOTIFY_REMOTE_CONNECTED = 2;
	
	public ConsoleCommand(int type, String message) {
		this.type = type;
		this.message = message;
	}
	
	public int getType() {
		return type;
	}
	
	public String getMessage() {
		return message;
	}

	public static ConsoleCommand getRemoteDisconnectedCommand() {
		return new ConsoleCommand(NOTIFY_REMOTE_DISCONNECTED, null);
	}

}
