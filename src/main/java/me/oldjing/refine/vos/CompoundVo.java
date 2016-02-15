package me.oldjing.refine.vos;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CompoundVo extends BasicVo {
	@SerializedName("has_fail")
	public final boolean hasFail;
	public final List<JsonObject> result;

	public CompoundVo(boolean hasFail, List<JsonObject> result) {
		this.hasFail = hasFail;
		this.success = true;
		this.result = result;
	}
}
