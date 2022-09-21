package like.ai.tcp;


import java.nio.charset.Charset;
import reactor.core.publisher.Flux;
import reactor.netty.tcp.TcpServer;

/**
 * @author chenaiquan
 */
public class EchoServer {

	public static void main(String[] args) {
		TcpServer.create().port(8080).wiretap(true).handle((in, out) -> {
			// 订阅者必须等于 2 才开始订阅 .publish().autoConnect(2);
			Flux<String> flux = in.receive().asString(Charset.defaultCharset()).publish().autoConnect(2);

			// 第一个订阅 去除 后置 换行符
			flux.subscribe(message -> {
				if (message.endsWith("\n")) {
					message = message.substring(0, message.length() - 2);
				}
				System.out.println("message [" + message + "]");
			});

			// 把请求的 message 响应给客户端
			return out.sendString(flux.map(item -> item)).neverComplete();
		}).bindNow().onDispose().block();
	}
}
