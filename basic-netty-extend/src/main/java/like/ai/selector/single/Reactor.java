package like.ai.selector.single;

import static like.ai.selector.reactor.HandlerAdaptor.adaptor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @author chenaiquan
 */
public class Reactor {

	private final Selector demultiplexer;

	private Reactor(ServerSocketChannel server) throws IOException {
		demultiplexer = Selector.open();
		server.register(demultiplexer, SelectionKey.OP_ACCEPT);
	}

	public void run() {
		try {
			// Loop indefinitely
			for (; ; ) {
				demultiplexer.select();
				Set<SelectionKey> readyHandles = demultiplexer.selectedKeys();
				Iterator<SelectionKey> handleIterator = readyHandles.iterator();

				while (handleIterator.hasNext()) {
					SelectionKey next = handleIterator.next();
					adaptor(next, demultiplexer).handle(next);
					handleIterator.remove();
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void startReactor(int port) throws Exception {
		ServerSocketChannel server = ServerSocketChannel.open();
		server.socket().bind(new InetSocketAddress(port));
		server.configureBlocking(false);
		Reactor reactor = new Reactor(server);
		reactor.run(); // Run the dispatcher loop
	}

}
