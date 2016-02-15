package me.oldjing.refine;

import com.google.gson.*;
import me.oldjing.refine.vos.CompoundVo;

import java.lang.reflect.Type;

public class CompoundDeserializer implements JsonDeserializer<CompoundVo> {

	@Override
	public CompoundVo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		JsonObject jObject = json.getAsJsonObject();
		JsonArray array = jObject.get("result").getAsJsonArray();

		// TODO: support parse compound result array value object
		return null;
	}
}
