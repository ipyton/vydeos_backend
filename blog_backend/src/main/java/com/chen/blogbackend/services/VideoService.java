package com.chen.blogbackend.services;

import com.chen.blogbackend.entities.*;
import com.chen.blogbackend.mappers.MovieDownloadRequestParser;
import com.chen.blogbackend.mappers.PlayableMapper;
import com.chen.blogbackend.mappers.SeasonMetaMapper;
import com.chen.blogbackend.mappers.VideoParser;
import com.chen.blogbackend.util.VideoUtil;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Service
public class VideoService {

    private static final Logger logger = LoggerFactory.getLogger(VideoService.class);

    @Autowired
    CqlSession session;

    @Autowired
    Jedis jedis;

    private PreparedStatement getVideoMeta;
    private PreparedStatement getGallery;
    private PreparedStatement collectVideo;
    private PreparedStatement unstarVideo;

    private PreparedStatement sendRequest;
    private PreparedStatement getRequest;
    private PreparedStatement getRequestById;
    private PreparedStatement isStared;
    private PreparedStatement getPlayable;
    private PreparedStatement isPlayable;
    private PreparedStatement getSeasonMeta;
    private PreparedStatement insertSeasonMeta;
    private PreparedStatement updateTotalEpisode;
    private PreparedStatement updateEpisodeMeta;
    private PreparedStatement addSeasonCount;
    private PreparedStatement getPlayable3;

    //private PreparedStatement getPlayableCount;
//movieId text, createTime timestamp, userId text
    @PostConstruct
    public void init() {
        logger.info("Initializing VideoService and preparing CQL statements");
        try {
            getVideoMeta = session.prepare("select * from movie.meta where resource_id = ? and type = ? and language = ?;");
            getGallery = session.prepare("select * from movie.movieGallery where user_id = ?;");
            collectVideo = session.prepare("insert into movie.movieGallery(resource_id, user_id, poster, introduction, " +
                    "movie_name, actor_list,release_year,language,type) values(?,?,?,?,?,?,?,?,?);");
            unstarVideo = session.prepare("delete from movie.movieGallery where user_id = ? and resource_id = ? and type = ? ;");
            sendRequest = session.prepare("insert into movie.requests (resource_id, type, create_time, userId, " +
                    "movie_name, actor_list, release_year,language) values (?, ?, ?, ?, ?, ?, ?, ?);");
            getRequest = session.prepare("select * from movie.requests;");
            getRequestById = session.prepare("select * from movie.requests where resource_id = ? and type = ?;");
            isStared = session.prepare("select * from movie.movieGallery where user_id = ? and resource_id = ? and type = ? ;");
            getPlayable = session.prepare("select * from movie.playable where resource_id = ? and type = ? and season_id=? and episode = ?;");
            //getPlayableCount = session.prepare("select count(*) from movie.playable where resource_id = ? and type = ? and e;");
            isPlayable = session.prepare("select * from movie.playable where resource_id = ? and type = ?;");
            getSeasonMeta = session.prepare("select * from movie.season_meta where resource_id = ? and type = ? and season_id=?;");
            //create table season_meta(resource_id text, type text, total_episode int, season_id int, primary key ( (resource_id, type, season_id) ) );
            insertSeasonMeta = session.prepare("insert into movie.season_meta (resource_id, type, total_episode, season_id) values (?,?,?,?);");
            updateTotalEpisode = session.prepare("update movie.season_meta set total_episode = ?  where resource_id = ? and type = ? and season_id=?;");
            addSeasonCount = session.prepare("update movie.meta set total_season = ?  where resource_id = ? and type = ? and language = ?;");
            getPlayable3 = session.prepare("select * from movie.playable where resource_id = ? and type = ? and season_id=?;");

            logger.info("VideoService initialization completed successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize VideoService prepared statements", e);
            throw e;
        }
    }

    public boolean starVideo(Video video) {
        logger.debug("starVideo method called with video: {}", video);
        return true;
    }

    public Video getVideo() {
        logger.debug("getVideo method called, returning new Video instance");
        return new Video();
    }

    public Video getVideoMeta(String resourceId, String type, String language) {
        logger.debug("Getting video metadata for resourceId: {}, type: {}, language: {}", resourceId, type, language);
        try {
            ResultSet execute = session.execute(getVideoMeta.bind(resourceId, type, language));
            List<Video> videos = VideoParser.videoMetaParser(execute);

            if (videos.isEmpty()) {
                logger.warn("No video metadata found for resourceId: {}, type: {}, language: {}", resourceId, type, language);
                return null;
            }

            logger.debug("Successfully retrieved video metadata for resourceId: {}", resourceId);
            return videos.get(0);
        } catch (Exception e) {
            logger.error("Error retrieving video metadata for resourceId: {}, type: {}, language: {}",
                    resourceId, type, language, e);
            throw e;
        }
    }

    // return user saved information.
    public List<Video> getGallery(String userId) {
        logger.debug("Getting gallery for userId: {}", userId);
        try {
            ResultSet execute = session.execute(getGallery.bind(userId));
            List<Video> videos = VideoParser.videoMetaParser(execute);
            logger.debug("Retrieved {} videos from gallery for userId: {}", videos.size(), userId);
            return videos;
        } catch (Exception e) {
            logger.error("Error retrieving gallery for userId: {}", userId, e);
            throw e;
        }
    }

    public boolean collectVideo(String userId, String videoId, String type, String language) {
        logger.info("Collecting video for userId: {}, videoId: {}, type: {}, language: {}",
                userId, videoId, type, language);
        try {
            Video videoMeta = getVideoMeta(videoId, type, language);
            if (videoMeta == null) {
                logger.warn("Cannot collect video - no metadata found for videoId: {}, type: {}, language: {}",
                        videoId, type, language);
                return false;
            }

            ResultSet execute = session.execute(collectVideo.bind(videoId, userId, videoMeta.getPoster(),
                    videoMeta.getIntroduction(), videoMeta.getMovieName(), videoMeta.getActorList(),
                    videoMeta.getReleaseYear(), language, type));

            boolean success = execute.getExecutionInfo().getErrors().isEmpty();
            if (success) {
                logger.info("Successfully collected video for userId: {}, videoId: {}", userId, videoId);
            } else {
                logger.error("Failed to collect video for userId: {}, videoId: {}, errors: {}",
                        userId, videoId, execute.getExecutionInfo().getErrors());
            }
            return success;
        } catch (Exception e) {
            logger.error("Error collecting video for userId: {}, videoId: {}, type: {}, language: {}",
                    userId, videoId, type, language, e);
            return false;
        }
    }

    public boolean unstarVideo(String userId, String resourceId, String type) {
        logger.info("Unstarring video for userId: {}, resourceId: {}, type: {}", userId, resourceId, type);
        try {
            ResultSet execute = session.execute(unstarVideo.bind(userId, resourceId, type));
            boolean success = execute.getExecutionInfo().getErrors().isEmpty();

            if (success) {
                logger.info("Successfully unstarred video for userId: {}, resourceId: {}", userId, resourceId);
            } else {
                logger.error("Failed to unstar video for userId: {}, resourceId: {}, errors: {}",
                        userId, resourceId, execute.getExecutionInfo().getErrors());
            }
            return success;
        } catch (Exception e) {
            logger.error("Error unstarring video for userId: {}, resourceId: {}, type: {}",
                    userId, resourceId, type, e);
            return false;
        }
    }

    public boolean sendRequest(String email, String resourceId, String type, String language) {
        logger.info("Sending download request for email: {}, resourceId: {}, type: {}, language: {}",
                email, resourceId, type, language);
        try {
            ResultSet execute1 = session.execute(getVideoMeta.bind(resourceId, type, "en-US"));
            List<Video> videos = VideoParser.videoMetaParser(execute1);

            if (videos.isEmpty()) {
                logger.warn("No video metadata found for download request - type: {}, resourceId: {}, email: {}, language: {}",
                        type, resourceId, email, language);
                return false;
            }

            Video video = videos.get(0);
            ResultSet execute = session.execute(sendRequest.bind(video.getResourceId(), type, Instant.now(), email,
                    video.getMovieName(), video.getActorList(), video.getReleaseYear(), "en-US"));

            boolean success = execute.getExecutionInfo().getErrors().isEmpty();
            if (success) {
                logger.info("Successfully sent download request for email: {}, resourceId: {}", email, resourceId);
            } else {
                logger.error("Failed to send download request for email: {}, resourceId: {}, errors: {}",
                        email, resourceId, execute.getExecutionInfo().getErrors());
            }
            return success;
        } catch (Exception e) {
            logger.error("Error sending download request for email: {}, resourceId: {}, type: {}, language: {}",
                    email, resourceId, type, language, e);
            return false;
        }
    }

    public List<MovieDownloadRequest> getRequests() {
        logger.debug("Retrieving all download requests");
        try {
            ResultSet execute = session.execute(getRequest.bind());
            List<MovieDownloadRequest> requests = MovieDownloadRequestParser.parseMovieDownloadRequest(execute);
            logger.debug("Retrieved {} download requests", requests.size());
            return requests;
        } catch (Exception e) {
            logger.error("Error retrieving download requests", e);
            throw e;
        }
    }

    public boolean isRequested(String movieId, String type) {
        logger.debug("Checking if movie is already requested - movieId: {}, type: {}", movieId, type);
        try {
            ResultSet execute = session.execute(getRequestById.bind(movieId, type));
            boolean requested = !execute.all().isEmpty();
            logger.debug("Movie request status for movieId: {}, type: {} - requested: {}", movieId, type, requested);
            return requested;
        } catch (Exception e) {
            logger.error("Error checking request status for movieId: {}, type: {}", movieId, type, e);
            throw e;
        }
    }

    public boolean isStared(String userId, String resourceId, String type) {
        logger.debug("Checking if video is starred - userId: {}, resourceId: {}, type: {}", userId, resourceId, type);
        try {
            ResultSet execute = session.execute(isStared.bind(userId, resourceId, type));
            boolean starred = !execute.all().isEmpty();
            logger.debug("Video star status for userId: {}, resourceId: {} - starred: {}", userId, resourceId, starred);
            return starred;
        } catch (Exception e) {
            logger.error("Error checking star status for userId: {}, resourceId: {}, type: {}",
                    userId, resourceId, type, e);
            throw e;
        }
    }

    public List<Playable> getPlayable(String resourceId, String type, Integer seasonId, Integer episode) {
        logger.debug("Getting playable content for resourceId: {}, type: {}, seasonId: {}, episode: {}",
                resourceId, type, seasonId, episode);
        try {
            ResultSet execute = session.execute(getPlayable.bind(resourceId, type, seasonId, episode));
            List<Playable> list = PlayableMapper.parse(execute);
            logger.debug("Retrieved {} playable items for resourceId: {}, seasonId: {}, episode: {}",
                    list.size(), resourceId, seasonId, episode);
            return list;
        } catch (Exception e) {
            logger.error("Error retrieving playable content for resourceId: {}, type: {}, seasonId: {}, episode: {}",
                    resourceId, type, seasonId, episode, e);
            throw e;
        }
    }

    public List<Playable> getPlayable(String resourceId, String type, Integer seasonId) {
        logger.debug("Getting all playable content for resourceId: {}, type: {}, seasonId: {}",
                resourceId, type, seasonId);
        try {
            ResultSet execute = session.execute(getPlayable3.bind(resourceId, type, seasonId));
            List<Playable> list = PlayableMapper.parse(execute);
            logger.debug("Retrieved {} playable items for resourceId: {}, seasonId: {}",
                    list.size(), resourceId, seasonId);
            return list;
        } catch (Exception e) {
            logger.error("Error retrieving playable content for resourceId: {}, type: {}, seasonId: {}",
                    resourceId, type, seasonId, e);
            throw e;
        }
    }

    public Integer getSeasons(String resourceId, String type) throws Exception {
        logger.debug("Getting season count for resourceId: {}, type: {}", resourceId, type);
        try {
            ResultSet execute = session.execute(getVideoMeta.bind(resourceId, type));
            List<Video> videos = VideoParser.videoMetaParser(execute);

            if (videos.isEmpty()) {
                logger.warn("No video found for season count - resourceId: {}, type: {}", resourceId, type);
                return null;
            }

            Video video = videos.get(0);
            if (video.getType() == null) {
                logger.error("Database corruption detected - no type found for resourceId: {}", resourceId);
                throw new Exception("No movie founded, the database corrupted!");
            } else if (video.getType().equals("movie")) {
                logger.debug("Resource is a movie, returning 0 seasons for resourceId: {}", resourceId);
                return 0;
            } else {
                logger.debug("Retrieved {} seasons for resourceId: {}", video.getTotalSeason(), resourceId);
                return video.getTotalSeason();
            }
        } catch (Exception e) {
            logger.error("Error getting season count for resourceId: {}, type: {}", resourceId, type, e);
            throw e;
        }
    }

    public Boolean isPlayable(String resourceId, String type) {
        logger.debug("Checking if content is playable - resourceId: {}, type: {}", resourceId, type);
        try {
            ResultSet execute = session.execute(isPlayable.bind(resourceId, type));
            boolean playable = !execute.all().isEmpty();
            logger.debug("Playable status for resourceId: {}, type: {} - playable: {}", resourceId, type, playable);
            return playable;
        } catch (Exception e) {
            logger.error("Error checking playable status for resourceId: {}, type: {}", resourceId, type, e);
            throw e;
        }
    }

    public SeasonMeta getSeasonMeta(String resourceId, String type, Integer seasonId, String language) throws Exception {
        logger.debug("Getting season metadata for resourceId: {}, type: {}, seasonId: {}, language: {}",
                resourceId, type, seasonId, language);

        if (resourceId == null || resourceId.isEmpty() || type == null || type.isEmpty() ||
                seasonId == null || seasonId < 1 || language == null) {
            logger.error("Invalid parameters for getSeasonMeta - resourceId: {}, type: {}, seasonId: {}, language: {}",
                    resourceId, type, seasonId, language);
            throw new Exception("lost params");
        }

        try {
            ResultSet execute = session.execute(getSeasonMeta.bind(resourceId, type, seasonId));
            List<SeasonMeta> seasonMetas = SeasonMetaMapper.parseSeasonMeta(execute);

            if (seasonMetas.isEmpty()) {
                logger.warn("No season metadata found for resourceId: {}, type: {}, seasonId: {}",
                        resourceId, type, seasonId);
                return null;
            }

            SeasonMeta seasonMeta = seasonMetas.get(0);
            List<Playable> playable = getPlayable(resourceId, type, seasonId);

            if (!playable.isEmpty()) {
                ArrayList<Integer> availablePlayable = new ArrayList<>();
                for (Playable playableItem : playable) {
                    availablePlayable.add(playableItem.getEpisode());
                }
                seasonMeta.setAvailableEpisodes(availablePlayable);
                logger.debug("Found {} available episodes for resourceId: {}, seasonId: {}",
                        availablePlayable.size(), resourceId, seasonId);
            }

            logger.debug("Successfully retrieved season metadata for resourceId: {}, seasonId: {}",
                    resourceId, seasonId);
            return seasonMeta;
        } catch (Exception e) {
            logger.error("Error retrieving season metadata for resourceId: {}, type: {}, seasonId: {}, language: {}",
                    resourceId, type, seasonId, language, e);
            throw e;
        }
    }

    public boolean addEpisode(String resourceId, String type, Integer seasonId) throws Exception {
        logger.info("Adding episode to resourceId: {}, type: {}, seasonId: {}", resourceId, type, seasonId);

        if (seasonId == null || seasonId <= 0 || type == null || type.isEmpty()) {
            logger.error("Invalid parameters for addEpisode - resourceId: {}, type: {}, seasonId: {}",
                    resourceId, type, seasonId);
            throw new Exception("Not enough parameters ");
        }

        try {
            SeasonMeta seasonMeta = getSeasonMeta(resourceId, type, seasonId, "en-US");
            if (seasonMeta == null) {
                logger.error("No season metadata found for addEpisode - type: {}, resourceId: {}, seasonId: {}",
                        type, resourceId, seasonId);
                throw new Exception("No season meta found for " + type + " " + resourceId + " " + seasonId);
            }

            int newEpisodeCount = seasonMeta.getTotalEpisode() + 1;
            updateEpisode(resourceId, type, seasonId, newEpisodeCount);
            logger.info("Successfully added episode to resourceId: {}, seasonId: {}, new total: {}",
                    resourceId, seasonId, newEpisodeCount);
            return true;
        } catch (Exception e) {
            logger.error("Error adding episode to resourceId: {}, type: {}, seasonId: {}",
                    resourceId, type, seasonId, e);
            throw e;
        }
    }

    private boolean updateEpisode(String resourceId, String type, Integer seasonId, Integer episode) {
        logger.debug("Updating episode count for resourceId: {}, type: {}, seasonId: {}, episode: {}",
                resourceId, type, seasonId, episode);

        if (seasonId == null || seasonId <= 0 || type == null || type.isEmpty()) {
            logger.error("Invalid parameters for updateEpisode - resourceId: {}, type: {}, seasonId: {}, episode: {}",
                    resourceId, type, seasonId, episode);
            return false;
        }

        try {
            session.execute(updateTotalEpisode.bind(episode, resourceId, type, seasonId));
            logger.debug("Successfully updated episode count for resourceId: {}, seasonId: {} to {}",
                    resourceId, seasonId, episode);
            return true;
        } catch (Exception e) {
            logger.error("Error updating episode count for resourceId: {}, type: {}, seasonId: {}, episode: {}",
                    resourceId, type, seasonId, episode, e);
            return false;
        }
    }

    public boolean addSeason(String resourceId, String type) throws Exception {
        logger.info("Adding season to resourceId: {}, type: {}", resourceId, type);

        if (type == null || type.isEmpty() || resourceId == null || resourceId.isEmpty()) {
            logger.error("Invalid parameters for addSeason - resourceId: {}, type: {}", resourceId, type);
            throw new Exception("no sufficient params");
        }

        try {
            Video videoMeta = getVideoMeta(resourceId, type, "en-US");
            if (videoMeta == null) {
                logger.error("No video metadata found for addSeason - type: {}, resourceId: {}", type, resourceId);
                throw new Exception("No video meta found for " + type + " " + resourceId);
            }

            int newSeasonId = videoMeta.getTotalSeason() + 1;
            ResultSet execute = session.execute(insertSeasonMeta.bind(resourceId, type, 1, newSeasonId));
            ResultSet execute1 = session.execute(addSeasonCount.bind(newSeasonId, resourceId, type, "en-US"));

            boolean success = execute.getExecutionInfo().getErrors().isEmpty();
            if (success) {
                logger.info("Successfully added season {} to resourceId: {}", newSeasonId, resourceId);
            } else {
                logger.error("Failed to add season to resourceId: {}, errors: {}",
                        resourceId, execute.getExecutionInfo().getErrors());
            }
            return success;
        } catch (Exception e) {
            logger.error("Error adding season to resourceId: {}, type: {}", resourceId, type, e);
            throw e;
        }
    }
}