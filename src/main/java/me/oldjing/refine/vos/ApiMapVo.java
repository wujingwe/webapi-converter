package me.oldjing.refine.vos;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Map;

public class ApiMapVo extends BasicVo {
	public final Map<String, ApiVo> data;

	public ApiMapVo(Map<String, ApiVo> data) {
		this.data = data;
		this.success = true;
	}

	public static class ApiVo {
		public final int maxVersion;
		public final int minVersion;
		public final String path;
		public final String requestFormat;

		public ApiVo(int maxVersion, int minVersion, String path, String requestFormat) {
			this.maxVersion = maxVersion;
			this.minVersion = minVersion;
			this.path = path;
			this.requestFormat = requestFormat;
		}

		public boolean jsonEncode() {
			return requestFormat != null && requestFormat.equals("json");
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == null) {
				return false;
			}
			if (obj == this) {
				return true;
			}
			if (obj.getClass() != getClass()) {
				return false;
			}
			ApiVo rhs = (ApiVo) obj;
			return new EqualsBuilder()
					.append(maxVersion, rhs.maxVersion)
					.append(minVersion, rhs.minVersion)
					.append(path, rhs.path)
					.append(requestFormat, rhs.requestFormat)
					.isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder(17, 37).
					append(maxVersion).
					append(minVersion).
					append(path).
					append(requestFormat).
					toHashCode();
		}
	}
}
