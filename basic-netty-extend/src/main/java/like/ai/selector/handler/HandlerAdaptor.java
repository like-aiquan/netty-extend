package like.ai.selector.handler;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import like.ai.selector.handler.AcceptEventHandler;
import like.ai.selector.handler.EventHandler;
import like.ai.selector.handler.ReadEventHandler;
import like.ai.selector.handler.WriteEventHandler;

/**
 * @author chenaiquan
 */
public class HandlerAdaptor {
	// Runtime.getRuntime().availableProcessors();
	private static final int cores = 4;
	private static final Selector[] subs = new Selector[cores];
	private static final AtomicInteger run = new AtomicInteger(0);

	static {
		for (int i = 0; i < cores; i++) {
			try {
				subs[i] = Selector.open();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}

		selectHolding();
	}

	public static void selectHolding() {
		for (Selector sub : subs) {
			new Thread(() -> {
				while (true) {
					try {
						sub.select();
						Set<SelectionKey> keys = sub.selectedKeys();
						handle(keys.iterator());
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();
		}

	}

	private static void handle(Iterator<SelectionKey> iterator) throws IOException {
		while (iterator.hasNext()) {
			SelectionKey key = iterator.next();
			adaptor(key).handle(key);
			iterator.remove();
		}
	}

	public static EventHandler adaptor(SelectionKey handle) {
		if (handle.isAcceptable()) {
			return new AcceptEventHandler(subs[run.getAndIncrement()], true);
		}

		if (handle.isReadable()) {
			return new ReadEventHandler(true);
		}

		if (handle.isWritable()) {
			return new WriteEventHandler();
		}
		throw new RuntimeException("handle type not support");
	}

	public static EventHandler adaptor(SelectionKey handle, Selector demultiplexer) {
		if (handle.isAcceptable()) {
			return new AcceptEventHandler(demultiplexer, false);
		}

		if (handle.isReadable()) {
			return new ReadEventHandler(false);
		}

		if (handle.isWritable()) {
			return new WriteEventHandler();
		}
		throw new RuntimeException("handle type not support");
	}
}
