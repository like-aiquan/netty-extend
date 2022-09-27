package like.ai.selector.handler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * @author chenaiquan
 */
public class ReadEventHandler implements EventHandler {
	private ByteBuffer inputBuffer = ByteBuffer.allocate(2048);
	private boolean wakeup;

	public ReadEventHandler(boolean wakeup) {
		this.wakeup = wakeup;
	}

	@Override
	public void handle(SelectionKey handle) throws IOException {
		SocketChannel socketChannel = (SocketChannel) handle.channel();

		// Read data from client
		socketChannel.read(inputBuffer);
		// Rewind the buffer to start reading from the beginning
		inputBuffer.flip();
		byte[] buffer = new byte[inputBuffer.limit()];
		inputBuffer.get(buffer);
		System.out.println("Received message from client : " + new String(buffer));
		// Rewind the buffer to the previous state.
		inputBuffer.flip();
		// Register the interest for writable readiness event for
		// this channel in order to echo back the message
		Selector sub = handle.selector();
		if (wakeup) {
			sub.wakeup();
		}
		socketChannel.register(sub, SelectionKey.OP_WRITE, inputBuffer);
	}
}
