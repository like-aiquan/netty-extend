package like.ai.selector.handler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * @author chenaiquan
 */
public class WriteEventHandler implements EventHandler {

	@Override
	public void handle(SelectionKey handle) throws IOException {
		SocketChannel channel = (SocketChannel) handle.channel();
		if (!channel.finishConnect()) {
			return;
		}
		ByteBuffer inputBuffer = (ByteBuffer) handle.attachment();
		channel.write(inputBuffer);
		// write 一般用于最后一步操作
		// 用完了就关闭 channel
		channel.close();
		System.out.println("send call back!");
	}
}
