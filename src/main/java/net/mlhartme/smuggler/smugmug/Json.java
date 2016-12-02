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
package net.mlhartme.smuggler.smugmug;

import com.google.gson.*;
import com.sun.jersey.api.client.WebResource;

public class Json {
    public static JsonArray arrayOpt(JsonObject object, String name) {
        JsonElement element;

        element = object.get(name);
        if (element == null) {
            return null;
        } else {
            return element.getAsJsonArray();
        }
    }

    public static String uris(JsonObject object, String name) {
        JsonElement uri;

        uri = Json.element(object, "Uris", name);
        if (uri.isJsonObject()) {
            // verbosity 2 or 2 - or a post request that does not honor the verbosity
            return Json.string(uri.getAsJsonObject(), "Uri");
        } else {
            return uri.getAsString();
        }
    }

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

    public static int integer(JsonObject obj, String name) {
        JsonElement e;

        e = element(obj, name);
        if (e.isJsonPrimitive()) {
            return e.getAsInt();
        } else {
            throw new IllegalArgumentException("field '" + name + "' is not an integer: " + obj);
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

    public static JsonElement element(JsonObject obj, String name, String sub) {
        return element(object(obj, name), sub);
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

        obj = new JsonObject();
        for (int i = 0; i < keyValues.length; i += 2) {
            obj.add(keyValues[i], new JsonPrimitive(keyValues[i + 1]));
        }
        return post(resource, obj.toString());
    }


    public static JsonObject post(WebResource.Builder resource, Object body) {
        String response;

        response = resource.post(String.class, body);
        return parse(response).getAsJsonObject();
    }

    public static JsonObject parse(String str) {
        return parser.parse(str).getAsJsonObject();
    }

    private static final JsonParser parser = new JsonParser();

}
