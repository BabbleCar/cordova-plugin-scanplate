package org.openalpr;

import org.json.JSONException;
import org.json.JSONObject;
import org.openalpr.model.Results;

/**
 * Alpr JNI Wrapper
 */
public class AlprJNIWrapper {

    /**
     * Recognize By File Path
     *
     * @param filepath File Path
     * @param country Country
     * @param region Region
     * @param topN Number Results
     * @param configFile Configuration File
     *
     * @return String
     */
    public native String recognizeFilepath(String filepath, String country, String region, int topN, String configFile);

    /**
     * Recognize By Pixel Data
     *
     * @param pixelData Data pixel
     * @param bytesPerPixel Bytes Per Pixel
     * @param imgWidth Image Width
     * @param imgHeight Image Height
     * @param country Country
     * @param region Region
     * @param topN Number Result
     * @param configFile Configuration File
     *
     * @return String
     */
    public native String recognizePixelData(byte[] pixelData, int bytesPerPixel, int imgWidth, int imgHeight, String country, String region, int topN, String configFile);

    /**
     * Get Version
     *
     * @return String version api
     */
    public native String version();

    static {
        System.loadLibrary("openalpr-native");
    }

    private int topN = 10;
    private String country = "eu";
    private String region = "";
    private String configFile = "";

    /**
     * Get Number Results
     *
     * @return int Number Results
     */
    public int getTopN() {
        return this.topN;
    }

    /**
     * Set Number Results
     *
     * @param topN Number Results
     */
    public void setTopN(int topN) {
        this.topN = topN;
    }

    /**
     * Get Country
     *
     * @return Country
     */
    public String getCountry() {
        return country;
    }

    /**
     * Set Country
     *
     * @param country String
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * Get Region
     *
     * @return Region
     */
    public String getRegion() {
        return region;
    }

    /**
     * Set Region
     *
     * @param region String Region
     */
    public void setRegion(String region) {
        this.region = region;
    }

    /**
     * Get Configuration File
     *
     * @return String
     */
    public String getConfigFile() {
        return configFile;
    }

    /**
     * Set Configuration File
     *
     * @param imgFilePath Image File Path
     */
    public void setConfigFile(String imgFilePath) {
        this.configFile = imgFilePath;
    }

    /**
     * recognize
     *
     * @param filepath Image File Path
     *
     * @return Results
     */
    public Results recognize(String filepath) {
        return jsonToResults(recognizeFilepath(filepath,getCountry(),getRegion(),getTopN(),getConfigFile()));
    }

    /**
     * recognize
     *
     * @param pixelData Pixel Data
     * @param bytesPerPixel Bytes Per Pixel
     * @param imgWidth Image Width
     * @param imgHeight Image Height
     *
     * @return Results
     */
    public Results recognize(byte[] pixelData, int bytesPerPixel, int imgWidth, int imgHeight) {
        return jsonToResults(recognizePixelData(pixelData,bytesPerPixel,imgWidth,imgHeight,getCountry(),getRegion(),getTopN(),getConfigFile()));
    }

    /**
     * Json Stirng To Results
     *
     * @param json string json
     * @return Results json
     */
    private Results jsonToResults (String json) {
        if(json != null && json.length() > 0) {
            try {
                return new Results(new JSONObject(json));
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
         * Create instance
         *
         * @param conf Path file configuration
         * @return instance
         */
        public synchronized static AlprJNIWrapper create(String conf) {
            if (instance == null) {
                instance = new AlprJNIWrapper();
                instance.setConfigFile(conf);
            }

            return instance;
        }

    }

}

