package me.itsmas.landclaims.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public final class Json
{
    private Json() {}

    private static final JsonParser PARSER = new JsonParser();

    public static JsonObject fromFile(File file)
    {
        try
        {
            return PARSER.parse(new FileReader(file)).getAsJsonObject();
        }
        catch (IOException ignored) {}

        return null;
    }
}
