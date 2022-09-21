package like.ai;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author chenaiquan
 */
public class ServerHandler extends ChannelInboundHandlerAdapter {


	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		String message = msg.toString();
		if (message.endsWith("\n")) {
			message = message.substring(0, message.length() - 2);
		}
		System.out.println("ctx [" + ctx + "] ---   msg [" + message + "]");
		super.channelRead(ctx, msg);
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		System.out.println("ctx [" + ctx + "] complete");
		super.channelReadComplete(ctx);
	}
}
