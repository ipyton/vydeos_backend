package com.chen.blogbackend.services;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ShardStatistics;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.chen.blogbackend.entities.*;
import com.chen.blogbackend.entities.GroupMessage;
import com.chen.blogbackend.entities.deprecated.SingleMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class SearchService {

    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);

    @Autowired
    private ElasticsearchClient client;

    int size = 5;

    public String createSchema() {
        logger.debug("Creating schema - method called");
        return "";
    }

    public List<App> searchApplicationByName(String name, int from) throws IOException {
        logger.info("Searching applications by name: '{}', from: {}", name, from);

        try {
            SearchResponse<App> response = client.search(s -> s
                            .index("App")
                            .query(q -> q
                                    .match(t -> t
                                            .field("name")
                                            .query(name)
                                    )
                            ).from(from).size(5),
                    App.class
            );

            ArrayList<App> result = new ArrayList<>();
            for (Hit<App> hit : response.hits().hits()) {
                result.add(hit.source());
            }

            logger.info("Found {} applications matching name: '{}'", result.size(), name);
            return result;

        } catch (IOException e) {
            logger.error("Error searching applications by name: '{}', from: {}", name, from, e);
            throw e;
        }
    }

    public boolean setApplicationIndex(App app) throws IOException, InterruptedException {
        logger.info("Indexing application with ID: {}", app.getAppId());

        try {
            IndexResponse response = client.index(i -> i
                    .index("products")
                    .id(app.getAppId())
                    .document(app)
            );

            response.result().wait(2000);
            ShardStatistics shards = response.shards();
            boolean success = shards.failed().intValue() == 0;

            if (success) {
                logger.info("Successfully indexed application with ID: {}", app.getAppId());
            } else {
                logger.warn("Failed to index application with ID: {}, failed shards: {}",
                        app.getAppId(), shards.failed().intValue());
            }

            return success;

        } catch (IOException | InterruptedException e) {
            logger.error("Error indexing application with ID: {}", app.getAppId(), e);
            throw e;
        }
    }

    public ArrayList<App> searchApplicationByDescription(String description) throws IOException {
        logger.info("Searching applications by description: '{}'", description);

        try {
            SearchResponse<App> response = client.search(s -> s
                            .index("App")
                            .query(q -> q
                                    .match(t -> t
                                            .field("description")
                                            .query(description)
                                    )
                            ),
                    App.class
            );

            ArrayList<App> result = new ArrayList<>();
            for (Hit<App> hit : response.hits().hits()) {
                result.add(hit.source());
            }

            logger.info("Found {} applications matching description: '{}'", result.size(), description);
            return result;

        } catch (IOException e) {
            logger.error("Error searching applications by description: '{}'", description, e);
            throw e;
        }
    }

    public List<Post> searchByArticle(String userId, String text, int from) throws IOException {
        logger.info("Searching articles for user: '{}', text: '{}', from: {}", userId, text, from);

        try {
            SearchResponse<Post> response = client.search(s -> s
                            .index("Article")
                            .query(q -> q
                                    .match(t -> t
                                            .field("content")
                                            .query(text)
                                    )
                            ).from(from).size(size),
                    Post.class
            );

            ArrayList<Post> result = new ArrayList<>();
            for (Hit<Post> hit : response.hits().hits()) {
                result.add(hit.source());
            }

            logger.info("Found {} articles for user: '{}', text: '{}'", result.size(), userId, text);
            return result;

        } catch (IOException e) {
            logger.error("Error searching articles for user: '{}', text: '{}', from: {}", userId, text, from, e);
            throw e;
        }
    }

    public boolean setArticleIndex(Post post) throws InterruptedException, IOException {
        logger.info("Indexing article with ID: {}", post.getPostID());

        try {
            IndexResponse response = client.index(i -> i
                    .index("products")
                    .id(post.getPostID().toString())
                    .document(post)
            );

            response.result().wait(2000);
            ShardStatistics shards = response.shards();
            boolean success = shards.failed().intValue() == 0;

            if (success) {
                logger.info("Successfully indexed article with ID: {}", post.getPostID());
            } else {
                logger.warn("Failed to index article with ID: {}, failed shards: {}",
                        post.getPostID(), shards.failed().intValue());
            }

            return success;

        } catch (IOException | InterruptedException e) {
            logger.error("Error indexing article with ID: {}", post.getPostID(), e);
            throw e;
        }
    }

    public List<Friend> searchByUser(String userName, int from) throws IOException {
        logger.info("Searching users by username: '{}', from: {}", userName, from);
        return getFriends(userName, from);
    }

    public List<Friend> searchByUserDescription(String description, int from) throws IOException {
        logger.info("Searching users by description: '{}', from: {}", description, from);
        return getFriends(description, from);
    }

    public List<SingleMessage> searchSingleMessage(String text, int from) throws IOException {
        logger.info("Searching single messages with text: '{}', from: {}", text, from);

        try {
            SearchResponse<SingleMessage> response = client.search(s -> s
                            .index("chat")
                            .query(q -> q
                                    .match(t -> t
                                            .field("text")
                                            .query(text)
                                    )
                            ).from(from).size(size),
                    SingleMessage.class
            );

            ArrayList<SingleMessage> result = new ArrayList<>();
            for (Hit<SingleMessage> hit : response.hits().hits()) {
                result.add(hit.source());
            }

            logger.info("Found {} single messages matching text: '{}'", result.size(), text);
            return result;

        } catch (IOException e) {
            logger.error("Error searching single messages with text: '{}', from: {}", text, from, e);
            throw e;
        }
    }

    public boolean setSearchSingleMessage(SingleMessage message) throws IOException, InterruptedException {
        logger.info("Indexing single message with ID: {}", message.getMessageId());

        try {
            IndexResponse response = client.index(i -> i
                    .index("chat")
                    .id(message.getMessageId())
                    .document(message)
            );

            response.result().wait(2000);
            ShardStatistics shards = response.shards();
            boolean success = shards.failed().intValue() == 0;

            if (success) {
                logger.info("Successfully indexed single message with ID: {}", message.getMessageId());
            } else {
                logger.warn("Failed to index single message with ID: {}, failed shards: {}",
                        message.getMessageId(), shards.failed().intValue());
            }

            return success;

        } catch (IOException | InterruptedException e) {
            logger.error("Error indexing single message with ID: {}", message.getMessageId(), e);
            throw e;
        }
    }

    private List<Friend> getFriends(String description, int from) throws IOException {
        logger.debug("Getting friends with description: '{}', from: {}", description, from);

        try {
            SearchResponse<Friend> response = client.search(s -> s
                            .index("user")
                            .query(q -> q
                                    .match(t -> t
                                            .field("userName")
                                            .query(description)
                                    )
                            ).from(from).size(size),
                    Friend.class
            );

            ArrayList<Friend> result = new ArrayList<>();
            for (Hit<Friend> hit : response.hits().hits()) {
                result.add(hit.source());
            }

            logger.debug("Found {} friends matching description: '{}'", result.size(), description);
            return result;

        } catch (IOException e) {
            logger.error("Error getting friends with description: '{}', from: {}", description, from, e);
            throw e;
        }
    }

    public boolean setUserIndex(Friend friend) throws IOException, InterruptedException {
        logger.info("Indexing user with ID: {}", friend.getUserId());

        try {
            IndexResponse response = client.index(i -> i
                    .index("products")
                    .id(friend.getUserId())
                    .document(friend)
            );

            response.result().wait(2000);
            ShardStatistics shards = response.shards();
            boolean success = shards.failed().intValue() == 0;

            if (success) {
                logger.info("Successfully indexed user with ID: {}", friend.getUserId());
            } else {
                logger.warn("Failed to index user with ID: {}, failed shards: {}",
                        friend.getUserId(), shards.failed().intValue());
            }

            return success;

        } catch (IOException | InterruptedException e) {
            logger.error("Error indexing user with ID: {}", friend.getUserId(), e);
            throw e;
        }
    }

    public List<GroupMessage> searchGroupMessage(String keyword, int from) throws IOException {
        logger.info("Searching group messages with keyword: '{}', from: {}", keyword, from);

        try {
            SearchResponse<GroupMessage> response = client.search(s -> s
                            .index("group_message")
                            .query(q -> q
                                    .match(t -> t
                                            .field("content")
                                            .query(keyword)
                                    )
                            ).from(from).size(size),
                    GroupMessage.class
            );

            ArrayList<GroupMessage> result = new ArrayList<>();
            for (Hit<GroupMessage> hit : response.hits().hits()) {
                result.add(hit.source());
            }

            logger.info("Found {} group messages matching keyword: '{}'", result.size(), keyword);
            return result;

        } catch (IOException e) {
            logger.error("Error searching group messages with keyword: '{}', from: {}", keyword, from, e);
            throw e;
        }
    }

    public boolean setGroupMessage(GroupMessage message) throws IOException, InterruptedException {
        logger.info("Indexing group message with ID: {}", message.getMessageId());

        try {
            IndexResponse response = client.index(i -> i
                    .index("chat")
                    .id(String.valueOf(message.getMessageId()))
                    .document(message)
            );

            response.result().wait(2000);
            ShardStatistics shards = response.shards();
            boolean success = shards.failed().intValue() == 0;

            if (success) {
                logger.info("Successfully indexed group message with ID: {}", message.getMessageId());
            } else {
                logger.warn("Failed to index group message with ID: {}, failed shards: {}",
                        message.getMessageId(), shards.failed().intValue());
            }

            return success;

        } catch (IOException | InterruptedException e) {
            logger.error("Error indexing group message with ID: {}", message.getMessageId(), e);
            throw e;
        }
    }
}