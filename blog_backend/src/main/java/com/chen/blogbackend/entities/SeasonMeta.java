package com.chen.blogbackend.entities;

import java.util.List;

public class SeasonMeta {

    private String resourceId;
    private String type;
    private Integer totalEpisode;
    private Integer seasonId;
    private List<Integer> availableEpisodes;

    public SeasonMeta() {
    }

    public SeasonMeta(String resourceId, String type, Integer totalEpisode, Integer seasonId) {
        this.resourceId = resourceId;
        this.type = type;
        this.totalEpisode = totalEpisode;
        this.seasonId = seasonId;
    }


    public List<Integer> getAvailableEpisodes() {
        return availableEpisodes;
    }

    public void setAvailableEpisodes(List<Integer> availableEpisodes) {
        this.availableEpisodes = availableEpisodes;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getTotalEpisode() {
        return totalEpisode;
    }

    public void setTotalEpisode(Integer totalEpisode) {
        this.totalEpisode = totalEpisode;
    }

    public Integer getSeasonId() {
        return seasonId;
    }

    public void setSeasonId(Integer seasonId) {
        this.seasonId = seasonId;
    }

    @Override
    public String toString() {
        return "SeasonMeta{" +
                "resourceId='" + resourceId + '\'' +
                ", type='" + type + '\'' +
                ", totalEpisode=" + totalEpisode +
                ", seasonId=" + seasonId +
                '}';
    }

    @Override
    public int hashCode() {
        int result = resourceId.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + seasonId.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SeasonMeta other = (SeasonMeta) obj;
        return resourceId.equals(other.resourceId) &&
                type.equals(other.type) &&
                seasonId.equals(other.seasonId);
    }
}
