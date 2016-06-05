package org.openalpr.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Candidate {

    private String plate;
    private Double confidence;
    private Integer matches_template;

    public Candidate(String plate, Double confidence, Integer matches_template) {
        this.plate = plate;
        this.confidence = confidence;
        this.matches_template = matches_template;
    }

    public Candidate(JSONObject jCandidate) {
        try {
            this.plate = jCandidate.getString("plate");
            this.confidence = jCandidate.getDouble("confidence");
            this.matches_template = jCandidate.getInt("matches_template");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getPlate() {
        return plate;
    }

    public Integer getMatches_template() {
        return matches_template;
    }

    public Double getConfidence() {
        return confidence;
    }
}