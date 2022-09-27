package like.ai.selector.handler;

import java.io.IOException;
import java.nio.channels.SelectionKey;

/**
 * @author chenaiquan
 */
public interface EventHandler {

	void handle(SelectionKey handle) throws IOException;
}
