package org.openalpr;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;
import org.openalpr.Util.Utils;
import org.openalpr.model.Results;

import java.io.File;

/**
 * Alpr JNI Wrapper
 */
public class AlprJNIWrapper {

    static {
        System.loadLibrary("openalpr-native");
    }

    protected int topN = 10;
    protected String country = "eu";
    protected String region = "";
    protected String configFile = "";

    /**
     *
     * @return
     */
    public int getTopN() {
        return topN;
    }

    /**
     *
     * @param topN
     */
    public void setTopN(int topN) {
        this.topN = topN;
    }

    /**
     *
     * @return
     */
    public String getCountry() {
        return country;
    }

    /**
     *
     * @param country
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     *
     * @return
     */
    public String getRegion() {
        return region;
    }

    /**
     *
     * @param region
     */
    public void setRegion(String region) {
        this.region = region;
    }

    /**
     *
     * @return
     */
    public String getConfigFile() {
        return configFile;
    }

    /**
     *
     * @param imgFilePath
     */
    public void setConfigFile(String imgFilePath) {
        this.configFile = imgFilePath;
    }

    /**
     *
     * @param filepath
     * @return
     */
    public Results recognize(String filepath) {
        return jsonToResults(recognizeFilepath(filepath,getCountry(),getRegion(),getTopN(),getConfigFile()));
    }

    /**
     *
     * @param pixelData
     * @param bytesPerPixel
     * @param imgWidth
     * @param imgHeight
     * @return
     */
    public Results recognize(byte[] pixelData, int bytesPerPixel, int imgWidth, int imgHeight) {
        return jsonToResults(recognizePixelData(pixelData,bytesPerPixel,imgWidth,imgHeight,getCountry(),getRegion(),getTopN(),getConfigFile()));
    }

    /**
     *
     * @param filepath
     * @param country
     * @param region
     * @param topN
     * @param configFile
     * @return
     */
    @SuppressWarnings("JniMissingFunction")
    public native String recognizeFilepath(String filepath, String country, String region, int topN, String configFile);

    /**
     *
     * @param pixelData
     * @param bytesPerPixel
     * @param imgWidth
     * @param imgHeight
     * @param country
     * @param region
     * @param topN
     * @param configFile
     * @return
     */
    @SuppressWarnings("JniMissingFunction")
    public native String recognizePixelData(byte[] pixelData, int bytesPerPixel, int imgWidth, int imgHeight, String country, String region, int topN, String configFile);

    /**
     *
     * @return
     */
    @SuppressWarnings("JniMissingFunction")
    public native String version();

    /**
     *
     * @param json
     * @return
     */
    private Results jsonToResults (String json) {
        if(json != null && json.length() > 0) {
            try {
                JSONObject jresults = new JSONObject(json);
                return new Results(jresults);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * Factory Class
     */
    public static class Factory {

        private Factory() {}
        static AlprJNIWrapper instance;

        /**
         *
         * @param context
         * @param androidDataDir
         * @param scr
         * @return
         */
        public synchronized static AlprJNIWrapper create(Context context, String androidDataDir, String scr) {
            if (instance == null) {
                instance = new AlprJNIWrapper();
                (new Utils()).copyAssetFolder(scr + File.separatorChar + "runtime_data", androidDataDir + File.separatorChar + "runtime_data", context.getAssets());
                instance.setConfigFile(androidDataDir + File.separatorChar + "runtime_data/openalpr.conf");
            }

            return instance;
        }

        /**
         *
         * @param context
         * @param androidDataDir
         * @return
         */
        public synchronized static AlprJNIWrapper create(Context context, String androidDataDir) {
            return create(context, androidDataDir, ".");
        }
    }

}

