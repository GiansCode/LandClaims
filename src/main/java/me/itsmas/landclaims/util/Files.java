package me.itsmas.landclaims.util;

import java.io.File;

public final class Files
{
    private Files() {}

    public static String getFileName(File file)
    {
        return file.getName().split("\\.")[0];
    }
}
