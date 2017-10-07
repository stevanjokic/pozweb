package util;

import java.lang.reflect.Type;

import org.bson.types.ObjectId;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class MongoObjectIdSerializer implements JsonSerializer<ObjectId>{

	@Override
	public JsonElement serialize(ObjectId idObj, Type type, JsonSerializationContext context) {
//		System.out.println(idObj + " " + type + " " +  context);
//		JsonObject objId = new JsonObject();
		JsonPrimitive objId = new JsonPrimitive(idObj.toString());
//		objId.addProperty("_id", idObj.toString());
		return objId;
	}

}
