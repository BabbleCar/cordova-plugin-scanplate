package org.openalpr.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Coordinate {

    private Integer x;
    private Integer y;

    public Coordinate(Integer x, Integer y) {
        this.x = x;
        this.y = y;
    }

    public Coordinate(JSONObject jCoordinate) {
        try {
            this.x = jCoordinate.getInt("x");
            this.y = jCoordinate.getInt("y");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Integer getX() {
        return x;
    }

    public Integer getY() {
        return y;
    }
}