package like.ai.selector.reactor;

import static like.ai.selector.reactor.HandlerAdaptor.adaptor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

/**
 * @author chenaiquan
 */
public class Reactor {
	private final ServerSocketChannel ssc;
	private final Selector acceptor;

	public Reactor() throws IOException {
		this(8080);
	}

	public Reactor(int port) throws IOException {
		ssc = ServerSocketChannel.open();
		ssc.configureBlocking(false);
		ssc.bind(new InetSocketAddress(port));
		acceptor = Selector.open();
	}

	public void run() throws Exception {
		while (true) {
			ssc.register(acceptor, SelectionKey.OP_ACCEPT);
			acceptor.select();
			acceptor.selectedKeys().removeIf(key -> {
				try {
					adaptor(key).handle(key);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				return true;
			});
		}
	}
}
