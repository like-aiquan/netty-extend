package like.ai;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * netty
 *
 * @author chenaiquan
 */
public class EchoNettyServer {

	private final int port;

	public EchoNettyServer(int port) {
		this.port = port;
	}

	/**
	 * netty 服务端启动
	 */
	public void action() throws InterruptedException {
		// 用来接收进来的连接
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		// 用来处理已经被接收的连接，一旦bossGroup接收到连接，就会把连接信息注册到workerGroup上
		EventLoopGroup workerGroup = new NioEventLoopGroup();

		try {
			// nio服务的启动类
			ServerBootstrap sbs = new ServerBootstrap();
			// 配置nio服务参数
			sbs.group(bossGroup, workerGroup)
					// 说明一个新的Channel如何接收进来的连接
					.channel(NioServerSocketChannel.class)
					// tcp最大缓存链接个数
					.option(ChannelOption.SO_BACKLOG, 128)
					// 保持连接
					.childOption(ChannelOption.SO_KEEPALIVE, true)
					// 打印日志级别
					.handler(new LoggingHandler(LogLevel.INFO))
					// .childHandler(new SslChannelInitializer(new OpenSslServerContext()))
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel socketChannel) throws Exception {
							// marshalling 序列化对象的解码
							// socketChannel.pipeline().addLast(MarshallingCodeFactory.buildDecoder());
							// marshalling 序列化对象的编码
							// socketChannel.pipeline().addLast(MarshallingCodeFactory.buildEncoder());
							// 网络超时时间
							// socketChannel.pipeline().addLast(new ReadTimeoutHandler(5));
							// 处理接收到的请求
							// 这里相当于过滤器，可以配置多个
							socketChannel.pipeline()
									// .addFirst(new SslChannelInitializer(new OpenSslServerContext()))
									.addLast(new StringDecoder())
									.addLast(new ServerHandler());
						}
					});
			System.err.println("server 开启--------------");
			// 绑定端口，开始接受链接
			ChannelFuture cf = sbs.bind(port).sync();

			// 开多个端口
			// ChannelFuture cf2 = sbs.bind(3333).sync();
			// cf2.channel().closeFuture().sync();

			// 等待服务端口的关闭；在这个例子中不会发生，但你可以优雅实现；关闭你的服务
			cf.channel().closeFuture().sync();
		}
		finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}


	/**
	 * 开启netty服务线程
	 */
	public static void main(String[] args) throws InterruptedException {
		//  telnet localhost 8080
		// send hello world
		// in console will print like
		//  ctx [ChannelHandlerContext(ServerHandler#0, [id: 0x55935e16, L:/0:0:0:0:0:0:0:1:8080 - R:/0:0:0:0:0:0:0:1:51916])] ---   msg [hello world]
		new EchoNettyServer(8080).action();
	}
}

// 一 . 用特定字符当做分隔符，例如：$_
//  （1） 将下列代码添加到 initChannel方法内
//   将双方约定好的分隔符转成buf
//	 ByteBuf bb = Unpooled.copiedBuffer("$_".getBytes(Constant.charset));
//	 socketChannel.pipeline().addLast(new DelimiterBasedFrameDecoder(1024, bb));
//	 将接收到信息进行解码，可以直接把msg转成字符串
//	 socketChannel.pipeline().addLast(new StringDecoder());
//
//	（2） 在 ServerHandler中的 channelRead方法中应该替换内容为
//	 如果把msg直接转成字符串，必须在服务中心添加 socketChannel.pipeline().addLast(new StringDecoder());
//	 String reqStr = (String)msg;
//	 System.err.println("server 接收到请求信息是："+reqStr);
//	 String respStr = new StringBuilder("来自服务器的响应").append(reqStr).append("$_").toString();
//	 // 返回给客户端响应
//	 ctx.writeAndFlush(Unpooled.copiedBuffer(respStr.getBytes()));
//
//	 (3) 因为分隔符是双方约定好的，在ClientNetty和channelRead中也应该有响应的操作
//
//
//	 二. 双方约定好是定长报文
//	 双方约定好定长报文为6，长度不足时服务端会一直等待直到6个字符，所以客户端不足6个字符时用空格补充；其余操作，参考分隔符的情况
//	 socketChannel.pipeline().addLast(new FixedLengthFrameDecoder(6));
//
//
//	 三. 请求分为请求头和请求体，请求头放的是请求体的长度；一般生产上常用的
//	 （1）通信双方约定好报文头的长度，先截取改长度，
//	 （2）根据报文头的长度读取报文体的内容