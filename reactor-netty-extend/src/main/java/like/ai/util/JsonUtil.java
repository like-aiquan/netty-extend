package like.ai.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

/**
 * @author chenaiquan
 */
public class JsonUtil {
	private static final ObjectMapper DEFAULT_MAPPER;
	private static final ObjectReader DEFAULT_READER;
	private static final ObjectWriter DEFAULT_WRITER;

	static {
		DEFAULT_MAPPER = new ObjectMapper();

		DEFAULT_MAPPER.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);
		DEFAULT_MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		// 不依赖 getter setter 序列化配置
		DEFAULT_MAPPER.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
		DEFAULT_MAPPER.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
		// 推荐使用下划线命名的方式序列化以及反序列化 Json 字符
		DEFAULT_MAPPER.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

		DEFAULT_READER = DEFAULT_MAPPER.reader();
		DEFAULT_WRITER = DEFAULT_MAPPER.writer();
	}

	public static ObjectMapper copy() {
		return DEFAULT_MAPPER.copy();
	}

	public static ObjectReader reader() {
		return DEFAULT_READER;
	}

	public static ObjectWriter writer() {
		return DEFAULT_WRITER;
	}

	public static <T> T read(Class<T> clazz, String src) {
		try {
			return reader().forType(clazz).readValue(src);
		}
		catch (Exception e) {
			return null;
		}
	}

	public static <T> String write(T obj) {
		try {
			return writer().writeValueAsString(obj);
		}
		catch (JsonProcessingException e) {
			return "";
		}
	}
}
