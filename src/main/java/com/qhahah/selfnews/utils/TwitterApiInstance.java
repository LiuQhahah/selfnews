package com.qhahah.selfnews.utils;

import com.twitter.clientlib.TwitterCredentialsBearer;
import com.twitter.clientlib.api.TwitterApi;
import org.springframework.stereotype.Component;

@Component
public class TwitterApiInstance {

    public TwitterApiInstance() {

    }

    public static final TwitterApi apiInstance = new TwitterApi(
new TwitterCredentialsBearer(System.getenv("TWITTER_BEARER_TOKEN"))
    );
}
