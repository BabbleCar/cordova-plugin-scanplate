package org.openalpr.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Result {

    private String plate;
    private Double confidence;
    private Double matches_template;
    private Integer plate_index;
    private String region;
    private Double region_confidence;
    private Double processing_time_ms;
    private Integer requested_topn;
    private ArrayList<Coordinate> coordinates;
    private ArrayList<Candidate> candidates;

    public Result(String plate, Double confidence, Double matches_template, Integer plate_index, String region, Double region_confidence, Double processing_time_ms, Integer requested_topn, ArrayList<Coordinate> coordinates, ArrayList<Candidate> candidates) {
        this.plate = plate;
        this.confidence = confidence;
        this.matches_template = matches_template;
        this.plate_index = plate_index;
        this.region = region;
        this.region_confidence = region_confidence;
        this.processing_time_ms = processing_time_ms;
        this.requested_topn = requested_topn;
        this.coordinates = coordinates;
        this.candidates = candidates;
    }

    public Result(JSONObject jResult) {
        try {
            this.plate              = jResult.getString("plate");
            this.confidence         = jResult.getDouble("confidence");
            this.matches_template   = jResult.getDouble("matches_template");
            this.plate_index        = jResult.getInt("plate_index");
            this.region             = jResult.getString("region");
            this.region_confidence  = jResult.getDouble("region_confidence");
            this.processing_time_ms = jResult.getDouble("processing_time_ms");
            this.requested_topn     = jResult.getInt("requested_topn");
            this.coordinates        = new ArrayList<Coordinate>();
            this.candidates         = new ArrayList<Candidate>();

            JSONArray aCoordinates = jResult.getJSONArray("coordinates");
            for (int i = 0; i < aCoordinates.length(); i++) {
                this.coordinates.add(new Coordinate(aCoordinates.getJSONObject(i)));
            }

            JSONArray aCandidates = jResult.getJSONArray("candidates");
            for (int i = 0; i < aCandidates.length(); i++) {
                this.candidates.add(new Candidate(aCandidates.getJSONObject(i)));
            }
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
    }

    public String getPlate() {
        return plate;
    }

    public Double getConfidence() {
        return confidence;
    }

    public Double getMatches_template() {
        return matches_template;
    }

    public String getRegion() {
        return region;
    }

    public Integer getPlate_index() {
        return plate_index;
    }

    public Double getRegion_confidence() {
        return region_confidence;
    }

    public Double getProcessing_time_ms() {
        return processing_time_ms;
    }

    public Integer getRequested_topn() {
        return requested_topn;
    }

    public ArrayList<Coordinate> getCoordinates() {
        return coordinates;
    }

    public ArrayList<Candidate> getCandidates() {
        return candidates;
    }
}