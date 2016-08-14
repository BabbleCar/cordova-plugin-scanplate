package org.openalpr.Util;

import android.content.res.AssetManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * OpenALPR utils.
 */
public class Utils {

    /**
     * Copy Asset Folder
     *
     * @param path
     * @param dest
     * @param assetManager
     */
    public void copyAssetFolder(String path, String dest, AssetManager assetManager) {
        try {
            String[] assets = assetManager.list(path);
            if (assets.length == 0) {
                InputStream in = assetManager.open(path);
                OutputStream out = new FileOutputStream(dest);
                copyFile(in, out);
                in.close();
                out.flush();
                out.close();
            } else {
                File dir = new File(dest);
                if (!dir.exists()) dir.mkdir();
                for (String fileOrDir : assets) {
                    copyAssetFolder(path + "/" + fileOrDir, dest + "/" + fileOrDir, assetManager);
                }
            }
        } catch (IOException ex) {
        }
    }

    /**
     * Copy File
     *
     * @param in
     * @param out
     * @throws IOException
     */
    private void copyFile(InputStream in, OutputStream out) throws IOException {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
    }

}
