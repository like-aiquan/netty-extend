package like.ai.http;

import io.netty.channel.ChannelOption;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

/**
 * reactor netty http 请求刷羊了个羊次数 注: header 的 t 代表 jwt 信息，抓包即可
 *
 * @author chenaiquan
 */
public class HttpFlowCatMatch {

	private static final AtomicInteger count = new AtomicInteger(0);

	private static final String jwt = "";

	private static final String agent = "";

	public static void main(String[] args) {
		Executors.newScheduledThreadPool(6).scheduleAtFixedRate(new DO(), 0, 1, TimeUnit.MICROSECONDS);
	}

	static HttpClient getHttpClient() {
		// ConnectionProvider provider = ConnectionProvider.builder("http")
		//				.maxConnections(100)
		//				.lifo()
		//				.build();
		//	return HttpClient.create(provider);

		return HttpClient.create()
				.baseUrl("https://cat-match.easygame2021.com")
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 100)
				.headers((headers) -> {
					headers.add("Host", "cat-match.easygame2021.com");
					headers.add("Connection", "close");
					headers.add("xweb_xhr", "1");
					headers.add("t", jwt);
					headers.add("User-Agent", agent);
					headers.add("Content-Type", "application/json");
					headers.add("Accept", "*/*");
					headers.add("Sec-Fetch-Site", "cross-site");
					headers.add("Sec-Fetch-Mode", "cors");
					headers.add("Sec-Fetch-Dest", "empty");
					headers.add("Accept-Language", "en-us,en");
					headers.add("Accept-Encoding", "gzip, deflate");
					headers.add("Content-Length", "2");
				})
				.responseTimeout(Duration.ofSeconds(5))
				.doOnRequest(
						(req, con) -> {
							System.out.println("request!");
						})
				.doOnRequestError(
						(req, e) -> {
							System.out.println("request error! " + e.getMessage());
						})
				.doOnResponseError(
						(response, e) -> {
							System.out.println("response error! " + e.getMessage());
						})
				.doAfterResponseSuccess(
						(response, conn) -> {
							int count = 0;
							if (HttpResponseStatus.OK.equals(response.status())) {
								count = HttpFlowCatMatch.count.incrementAndGet();
							}
							System.out.println("success! st [" + response.status() + "], count [" + count + "]");
						});
	}

	public static class DO implements Runnable {
		@Override
		public void run() {
			try {
				// doGet();
				reactiveGet();
			}
			catch (Exception e) {
				System.out.println("error! " + e.getMessage());
			}
		}

		private void reactiveGet() {
			ReactiveHttpUtil.get("https://cat-match.easygame2021.com",
					"sheep/v1/game/game_over?rank_score=1&rank_state=1&rank_time=1314&rank_role=1&skin=1", null);
		}

		private static void doGet() {
			getHttpClient()
					.get()
					.uri("/sheep/v1/game/game_over?rank_score=1&rank_state=1&rank_time=1314&rank_role=1&skin=1")
					.response()
					.block();
		}
	}
}
