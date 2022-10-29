import com.google.common.collect.Lists;
import java.util.List;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * @author chenaiquan
 */
public class ReactiveTest {

	@Test
	void test1() {
		List<String> list = Lists.newArrayList("aaa", "bbb", "ccc");

		Flux.fromStream(list.stream()).switchIfEmpty(Flux.just("12345")).doOnNext(System.out::println).subscribe();
	}

	/**
	 * 尝试获取一个数字
	 */
	private Mono<Integer> getNum() {
//		boolean result = new Random().nextBoolean();
		if (false) {
			return Mono.just(1);
		}
		else {
			return Mono.empty();
		}
	}

	/**
	 * 创建一个数字
	 */
	private Mono<Integer> createNum() {
		System.out.println("create num");
		return Mono.just(5);
	}

	/**
	 * 保存一个数字
	 */
	private void saveNum(Integer num) {
		System.out.println("save num ---> " + num);
	}

	private Mono<Void> createIfNotExist() {
		return getNum()
				.switchIfEmpty(createNum())
				.doOnNext(this::saveNum)
				.then();
	}

	@Test
	void test() {
		StepVerifier.create(createIfNotExist()).verifyComplete();
	}

}
