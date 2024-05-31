package com.chen.blogbackend.services;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ShardStatistics;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.chen.blogbackend.entities.*;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class SearchService {

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Autowired
    private ElasticsearchClient client;

    int size = 5;


    public String createSchema() {
        return "";
    }

    public List<App> searchApplicationByName(String name, int from) throws IOException {
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
        return result;
    }

    public boolean setApplicationIndex(App app) throws IOException, InterruptedException {
        IndexResponse response = client.index(i -> i
                .index("products")
                .id(app.getAppId())
                .document(app)
        );
        response.result().wait(2000);
        ShardStatistics shards = response.shards();
        return shards.failed().intValue() == 0;
    }


    public ArrayList<App> searchApplicationByDescription(String description) throws IOException {
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
        return result;
    }

    public List<Post> searchByArticle(String userId, String text, int from) throws IOException {
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
        return result;
    }

    public boolean setArticleIndex(Post post) throws InterruptedException, IOException {
        IndexResponse response = client.index(i -> i
                .index("products")
                .id(post.getPostID())
                .document(post)
        );
        response.result().wait(2000);
        ShardStatistics shards = response.shards();
        return shards.failed().intValue() == 0;
    }

    public List<Friend> searchByUser(String userName, int from) throws IOException {
        return getFriends(userName, from);
    }

    public List<Friend> searchByUserDescription(String description, int from) throws IOException {
        return getFriends(description, from);
    }

    public List<SingleMessage> searchSingleMessage(String text, int from) throws IOException {
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
        return result;

    }

    public boolean setSearchSingleMessage(SingleMessage message) throws IOException, InterruptedException {
        IndexResponse response = client.index(i -> i
                .index("chat")
                .id(message.getMessageId())
                .document(message)
        );
        response.result().wait(2000);
        ShardStatistics shards = response.shards();
        return shards.failed().intValue() == 0;
    }



    private List<Friend> getFriends(String description, int from) throws IOException {
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
        return result;
    }


    public boolean setUserIndex(Friend friend) throws IOException, InterruptedException {
        IndexResponse response = client.index(i -> i
                .index("products")
                .id(friend.getUserId())
                .document(friend)
        );
        response.result().wait(2000);
        ShardStatistics shards = response.shards();
        return shards.failed().intValue() == 0;
    }

    public List<GroupMessage> searchGroupMessage(String keyword, int from) throws IOException {
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
        return result;
    }

    public boolean setGroupMessage(GroupMessage message) throws IOException, InterruptedException {
        IndexResponse response = client.index(i -> i
                .index("chat")
                .id(message.getMessageId())
                .document(message)
        );
        response.result().wait(2000);

        ShardStatistics shards = response.shards();
        return shards.failed().intValue() == 0;
    }

}
