/*
 * Copyright Michael Hartmeier
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.mlhartme.smuggler;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.sun.jersey.api.client.WebResource;

public class Json {
    public static String string(JsonObject obj, String name, String sub, String subsub) {
        return string(object(obj, name, sub), subsub);
    }
    public static String string(JsonObject obj, String name, String sub) {
        return string(object(obj, name), sub);
    }

    public static String string(JsonObject obj, String name) {
        JsonElement e;

        e = element(obj, name);
        if (e.isJsonPrimitive()) {
            return e.getAsString();
        } else {
            throw new IllegalArgumentException("field '" + name + "' is not a string: " + obj);
        }
    }

    public static JsonObject object(JsonObject obj, String name, String sub) {
        return object(object(obj, name), sub);
    }

    public static JsonObject object(JsonObject obj, String name) {
        JsonElement e;

        e = element(obj, name);
        if (e.isJsonObject()) {
            return e.getAsJsonObject();
        } else {
            throw new IllegalArgumentException("field '" + name + "' is not an object: " + obj);
        }
    }

    public static JsonElement element(JsonObject obj, String name) {
        JsonElement e;

        e = obj.get(name);
        if (e == null) {
            throw new IllegalArgumentException("field '" + name + "' not found: " + obj);
        }
        return e;
    }

    //--

    public static JsonObject post(WebResource.Builder resource, String ... keyValues) {
        JsonObject obj;
        String response;

        obj = new JsonObject();
        for (int i = 0; i < keyValues.length; i += 2) {
            obj.add(keyValues[i], new JsonPrimitive(keyValues[i + 1]));
        }
        response = resource.post(String.class, obj.toString());
        return new JsonParser().parse(response).getAsJsonObject();
    }
}
