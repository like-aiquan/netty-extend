package like.ai.tcp;


import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicReference;
import reactor.core.publisher.Flux;
import reactor.netty.tcp.TcpServer;

/**
 * @author chenaiquan
 */
public class EchoServer {

	public static void main(String[] args) {
		TcpServer.create()
				.port(8080)
				.wiretap(true)
				.handle((in, out) -> {
					Flux<String> flux = in.receive().asString(Charset.defaultCharset());
					// 订阅者必须大于 2 才开始订阅
					flux = flux.publish().autoConnect(2);
					// 第一个订阅 去除 后置 换行符
					flux.subscribe(message -> {
						if (message.endsWith("\n")) {
							message = message.substring(0, message.length() - 2);
						}
						// 打印 message
						System.out.println("message [" + message + "]");
					});
					// 把请求的 message 响应给客户端  flux 的 第二次订阅
					return out.sendString(flux.map(item -> item)).neverComplete();
				})
				.bindNow()
				.onDispose()
				.block();
	}
}
