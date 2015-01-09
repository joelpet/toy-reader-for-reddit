package se.joelpet.android.toyredditreader.gson;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import se.joelpet.android.toyredditreader.domain.SubredditWrapperListing;

public class ListingDeserializer implements JsonDeserializer<SubredditWrapperListing> {

    private static final Gson GSON = new Gson();

    @Override
    public SubredditWrapperListing deserialize(JsonElement json, Type typeOfT,
            JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        JsonElement data = jsonObject.get("data");
        return GSON.fromJson(data, SubredditWrapperListing.class);
    }
}
