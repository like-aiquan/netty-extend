package like.ai.http;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;
import like.ai.util.JsonUtil;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.util.Logger;
import reactor.util.Loggers;

/**
 * reactor http util
 *
 * @author chenaiquan
 */
public class ReactiveHttpUtil {
	private static final Logger log = Loggers.getLogger(ReactiveHttpUtil.class);
	private static final HttpClient HTTP_CLIENT;
	/**
	 * TODO change it， do not use new pool，must in a common thread pool submit task
	 */
	private static final ScheduledExecutorService pool;
	private static boolean logBody = true;

	static {
		ConnectionProvider provider = ConnectionProvider.builder("http").build();
		HTTP_CLIENT = HttpClient.create(provider)
				.responseTimeout(Duration.ofSeconds(5))
				.doOnRequest(
						(request, connection) -> {

						})
				.doOnRequestError(
						(req, e) -> {
							log.error("request failed. retry this! {}", e.getMessage(), e);
						})
				.doOnResponseError(
						(res, e) -> {
							log.error("why response error? {}", e.getMessage(), e);
						})
				.doAfterResponseSuccess(
						(res, conn) -> {
							if (!HttpResponseStatus.OK.equals(res.status())) {
								throw new RuntimeException(
										"status not support! " + res.status() + " path:" + res.uri());
							}
						});

		ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("http-thread-pool-%d").build();
		pool = Executors.newScheduledThreadPool(1, threadFactory);
	}

	public boolean isLogBody() {
		return logBody;
	}

	public ReactiveHttpUtil setLogBody(boolean logBody) {
		ReactiveHttpUtil.logBody = logBody;
		return this;
	}

	/**
	 * synchronized post conn, body not allow is a string with the escape character
	 */
	public static <T> T post(String baseUrl, String uri, Object body, Class<T> clazz) {
		return post(baseUrl, uri, null, body, clazz, false);
	}

	/**
	 * async post conn, body not allow is a string with the escape character
	 */
	public static <T> T asyncPost(String baseUrl, String uri, Object body, Class<T> clazz) {
		return post(baseUrl, uri, null, body, clazz, true);
	}

	/**
	 * maybe need to customize the header and change whether the synchronization, body not allow is a string with the
	 * escape character
	 */
	public static <T> T post(String baseUrl, String uri,
			Map<String, Object> headers, Object body, Class<T> clazz, boolean async) {
		String response = async ? asyncPost(baseUrl, uri, headers, body) : post(baseUrl, uri, headers, body);
		return JsonUtil.read(clazz, response);
	}

	/**
	 * body not allow is a string with the escape character
	 */
	public static <T> String post(String baseUrl, String uri, Map<String, Object> headers, T body) {
		return httpPost(baseUrl, uri, headers, body);
	}

	/**
	 * body not allow is a string with the escape character
	 */
	public static <T> String asyncPost(String baseUrl, String uri, Map<String, Object> headers, T body) {
		// TODO maybe reactor limit， invoke this get first result should be in a UnBlocking Thread;
		// TODO @{link reactor.core.publisher.BlockingSingleSubscriber.blockingGet()}
		CompletableFuture<String> future = CompletableFuture.supplyAsync(
				() -> httpPost(baseUrl, uri, headers, body), pool
		);
		return asyncResponse(future);
	}

	private static String asyncResponse(CompletableFuture<String> future) {
		try {
			return future.get();
		}
		catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * body not allow is a string with the escape character
	 */
	private static <T> String httpPost(String baseUrl, String uri, Map<String, Object> headers, T body) {
		try {
			String data = JsonUtil.write(body);
			if (logBody) {
				log.info("http post send to [{}], body {}", baseUrl + uri, data);
			}
			return HTTP_CLIENT
					.baseUrl(baseUrl)
					.headers(postDefaultHeaders(data))
					.headers(buildHeaders(headers))
					.post()
					.uri(uri)
					.send(ByteBufFlux.fromString(Mono.just(data)))
					.responseContent()
					.aggregate()
					.asString()
					.block();
		}
		catch (Exception e) {
			log.error(e.toString(), e);
			return "";
		}
	}

	/**
	 * post 请求特殊的 header
	 */
	private static Consumer<HttpHeaders> postDefaultHeaders(String data) {
		return header -> {
			// post 请求一定要带上 "Content-Length" 否则 某些情况下会报错
			header.add("Content-Length", data.getBytes().length);
		};
	}

	/**
	 * synchronized get conn
	 */
	public static <T> T get(String baseUrl, String uri, Class<T> clazz) {
		return get(baseUrl, uri, null, clazz, false);
	}

	/**
	 * async get coon
	 */
	public static <T> T asyncGet(String baseUrl, String uri, Class<T> clazz) {
		return get(baseUrl, uri, null, clazz, true);
	}

	public static <T> T get(String baseUrl, String uri, Map<String, Object> headers, Class<T> clazz, boolean async) {
		String response = async ? asyncGet(baseUrl, uri, headers) : get(baseUrl, uri, headers);
		return JsonUtil.read(clazz, response);
	}

	private static String get(String baseUrl, String uri, Map<String, Object> headers) {
		try {
			return HTTP_CLIENT
					.baseUrl(baseUrl)
					.headers(buildHeaders(headers))
					.get()
					.uri(uri)
					.responseContent()
					.aggregate()
					.asString(StandardCharsets.UTF_8)
					.block(Duration.ofSeconds(5));
		}
		catch (Exception e) {
			log.error(e.getMessage(), e);
			return "";
		}
	}

	private static Consumer<? super HttpHeaders> buildHeaders(Map<String, Object> headers) {
		return header -> {
			header.add("Content-Type", "application/json");
			Optional.ofNullable(headers).orElse(new HashMap<>(0)).forEach(header::add);
		};
	}

	private static String asyncGet(String baseUrl, String uri, Map<String, Object> headers) {
		CompletableFuture<String> future = CompletableFuture.supplyAsync(
				() -> get(baseUrl, uri, headers), pool
		);
		return asyncResponse(future);
	}

}
