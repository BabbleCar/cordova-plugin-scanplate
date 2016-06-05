package org.openalpr;

import org.json.JSONException;
import org.json.JSONObject;
import org.openalpr.model.Results;

public class AlprJNIWrapper {

    static {
        System.loadLibrary("openalpr-native");
    }

    protected int topN = 10;
    protected String country = "eu";
    protected String region = "";
    protected String configFile = "";

    public int getTopN() {
        return topN;
    }
    public void setTopN(int topN) {
        this.topN = topN;
    }

    public String getCountry() {
        return country;
    }
    public void setCountry(String country) {
        this.country = country;
    }

    public String getRegion() {
        return region;
    }
    public void setRegion(String region) {
        this.region = region;
    }

    public String getConfigFile() {
        return configFile;
    }
    public void setConfigFile(String imgFilePath) {
        this.configFile = imgFilePath;
    }

    public Results recognize(String filepath) {
        return jsonToResults(recognizeFilepath(filepath,getCountry(),getRegion(),getTopN(),getConfigFile()));
    }

    public Results recognize(byte[] pixelData, int bytesPerPixel, int imgWidth, int imgHeight) {
        return jsonToResults(recognizePixelData(pixelData,bytesPerPixel,imgWidth,imgHeight,getCountry(),getRegion(),getTopN(),getConfigFile()));
    }

    public native String recognizeFilepath(String filepath, String country, String region, int topN, String configFile);
    public native String recognizePixelData(byte[] pixelData, int bytesPerPixel, int imgWidth, int imgHeight, String country, String region, int topN, String configFile);
    public native String version();
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
}

