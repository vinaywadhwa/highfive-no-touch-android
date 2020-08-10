package com.vwap.high5_notouch;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import java.io.File;
import java.io.FileNotFoundException;

public class ImageProvider extends ContentProvider
{
    @Override
    public ParcelFileDescriptor openFile(Uri aUri, String aMode) throws FileNotFoundException
    {
        File file = new File(getContext().getFilesDir(), aUri.getPath());

        if (file.exists())
        {
            return (ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY));
        }

        throw new FileNotFoundException(aUri.getPath());
    }

    @Override
    public boolean onCreate()
    {
        return false;
    }

    @Override
    public int delete(Uri aUri, String aSelection, String[] aSelectionArgs)
    {
        return 0;
    }

    @Override
    public String getType(Uri aUri)
    {
        return null;
    }

    @Override
    public Uri insert(Uri aUri, ContentValues aValues)
    {
        return null;
    }

    @Override
    public Cursor query(Uri aUri, String[] aProjection, String aSelection, String[] aSelectionArgs, String aSortOrder)
    {
        return null;
    }

    @Override
    public int update(Uri aUri, ContentValues aValues, String aSelection, String[] aSelectionArgs)
    {
        return 0;
    }
}