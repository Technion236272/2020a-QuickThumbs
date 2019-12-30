package androidCourse.technion.quickthumbs;

import android.content.Context;
import android.os.Environment;

import androidCourse.technion.quickthumbs.personalArea.PersonalTexts.TextDataRow;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class CacheHandler {
    //Get Cache Folder in Android
    public static File getCacheFolder(Context context) {
        File cacheDir = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            cacheDir = new File(Environment.getExternalStorageDirectory(), "cachefolder");
            if(!cacheDir.isDirectory()) {
                cacheDir.mkdirs();
            }
        }

        if(!cacheDir.isDirectory()) {
            cacheDir = context.getCacheDir(); //get system cache folder
        }

        return cacheDir;
    }

    //Get App Data Folder in Android
    public static File getDataFolder(Context context) {
        File dataDir = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            dataDir = new File(Environment.getExternalStorageDirectory(), "myappdata");
            if(!dataDir.isDirectory()) {
                dataDir.mkdirs();
            }
        }

        if(!dataDir.isDirectory()) {
            dataDir = context.getFilesDir();
        }

        return dataDir;
    }

    public static void writeFileIntoCacheFolder(Context context, TextDataRow textDataRow) throws IOException {
        byte[] insert =  serializeObject(textDataRow);
        // open input stream test.txt for reading purpose.
        File cacheDir = getCacheFolder(context);
        InputStream inStream = new FileInputStream(cacheDir.getPath());
        InputStream inputStream = new BufferedInputStream(inStream, 10240);
        File cacheFile = new File(cacheDir, "textCache");
        FileOutputStream outputStream = new FileOutputStream(cacheFile);

        byte buffer[] = new byte[1024];
        int dataSize;
        int loadedSize = 0;
        while ((dataSize = inputStream.read(buffer)) != -1) {
            loadedSize += dataSize;
            outputStream.write(buffer, 0, dataSize);
        }

        outputStream.close();
    }

    public static TextDataRow loadFileFromCacheFolder(Context context) throws IOException, ClassNotFoundException {
        File cacheDir = getCacheFolder(context);
        File cacheFile = new File(cacheDir, "textCache");
        InputStream fileInputStream = new FileInputStream(cacheFile);
        byte buffer[] = new byte[1024];
        int read = fileInputStream.read(buffer, 0, 1024);
        TextDataRow item=(TextDataRow)deserializeBytes(buffer);
        return item;
    }

    public static byte[] serializeObject(Object obj) throws IOException
    {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bytesOut);
        oos.writeObject(obj);
        oos.flush();
        byte[] bytes = bytesOut.toByteArray();
        bytesOut.close();
        oos.close();
        return bytes;
    }

    public static Object deserializeBytes(byte[] bytes) throws IOException, ClassNotFoundException
    {
        ByteArrayInputStream bytesIn = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bytesIn);
        Object obj = ois.readObject();
        ois.close();
        return obj;
    }
}
