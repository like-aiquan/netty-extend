package like.ai.selector.handler;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * @author chenaiquan
 */
public class AcceptEventHandler implements EventHandler {
	private Selector sub;
	private boolean wakeup;

	public AcceptEventHandler(Selector sub, boolean wakeup) {
		this.sub = sub;
		this.wakeup = wakeup;
	}

	@Override
	public void handle(SelectionKey handle) throws IOException {
		System.out.println("acceptor event.. start");
		ServerSocketChannel channel = (ServerSocketChannel) handle.channel();
		SocketChannel accept = channel.accept();
		accept.configureBlocking(false);
		if (wakeup) {
			sub.wakeup();
		}
		accept.register(sub, SelectionKey.OP_READ);
	}
}
