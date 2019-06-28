package com.example.flixster.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Config {

    // base url for loading images
    String imageBaseUrl;
    // poster size to use when fetching img , part of url
    String posterSize;
    // backdrop size for landscape orientation
    String backdropSize;

    public Config(JSONObject object) throws JSONException {
        // parse outer images object to access inner data
        JSONObject images = object.getJSONObject("images");
        //get image base url
        imageBaseUrl = images.getString("secure_base_url");
        // get poster size to use when fetching image
        JSONArray posterSizeOptions =  images.getJSONArray("poster_sizes");
        // use the poster size option w342 as a fallback located at index 3!
        posterSize = posterSizeOptions.optString(3, "w342");
        // parse  backdrop sizes and use the landscape option which is at index 1 and w780 as fallback
        JSONArray backdropSizeOptions = images.getJSONArray("backdrop_sizes");
    }

    // helper function for constructing urls
    public String getImageUrl(String size, String path) {
        return String.format("%s%s%s", imageBaseUrl, size, path); // concatenate all three pieces of url
    }

    public String getImageBaseUrl() {
        return imageBaseUrl;
    }

    public String getPosterSize() {
        return posterSize;
    }
}
