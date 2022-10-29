package like.ai.tcp;


import java.nio.charset.Charset;
import java.util.Optional;
import reactor.core.publisher.Flux;
import reactor.netty.tcp.TcpServer;

/**
 * @author chenaiquan
 */
public class EchoServer {

	private static final int port;

	static {
		port = Integer.parseInt(Optional.ofNullable(System.getProperty("server.port")).orElse("8011"));
	}

	public static void main(String[] args) {
		TcpServer.create()
				.port(port)
				.wiretap(true)
				.handle((in, out) -> {
					Flux<String> flux = in.receive().asString(Charset.defaultCharset())
					// 第一个订阅 去除 后置 换行符
					.doOnNext(message -> {
						if (message.endsWith("\n")) {
							message = message.substring(0, message.length() - 2);
						}
						// 打印 message
						System.out.println("message [" + message + "]");
					});
					// 把请求的 message 响应给客户端  flux 的 第二次订阅
					return out.sendString(flux).neverComplete();
				})
				.bindNow()
				.onDispose()
				.block();
	}
}
