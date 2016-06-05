package org.openalpr.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Results {

    private int version;
    private String data_type;
    private Double epoch_time;
    private Integer img_width;
    private Integer img_height;
    private Double processing_time_ms;
    private String regions_of_interest;
    private ArrayList<Result> results;

    public Results(int version, String data_type, Double epoch_time, Integer img_width, Integer img_height, Double processing_time_ms, String regions_of_interest, ArrayList<Result> results) {
        this.version = version;
        this.data_type = data_type;
        this.epoch_time = epoch_time;
        this.img_width = img_width;
        this.img_height = img_height;
        this.processing_time_ms = processing_time_ms;
        this.regions_of_interest = regions_of_interest;
        this.results = results;
    }

    public Results(JSONObject jResults) {
        try {
            this.version             = jResults.getInt("version");
            this.data_type           = jResults.getString("data_type");
            this.epoch_time          = jResults.getDouble("epoch_time");
            this.img_width           = jResults.getInt("img_width");
            this.img_height          = jResults.getInt("img_height");
            this.processing_time_ms  = jResults.getDouble("processing_time_ms");
            this.regions_of_interest = jResults.getString("regions_of_interest");
            this.results             = new ArrayList<>();

            JSONArray aResult = jResults.getJSONArray("results");
            for (int i = 0; i < aResult.length(); i++) {
                this.results.add(new Result(aResult.getJSONObject(i)));
            }
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
    }

    public String getData_type() {
        return data_type;
    }

    public int getImg_width() {
        return img_width;
    }

    public int getImg_height() {
        return img_height;
    }

    public String getRegions_of_interest() {
        return regions_of_interest;
    }

    public int getVersion() {
        return version;
    }

    public Double getEpoch_time() {
        return epoch_time;
    }

    public Double getProcessing_time_ms() {
        return processing_time_ms;
    }

    public ArrayList<Result> getResults() {
        return results;
    }

}