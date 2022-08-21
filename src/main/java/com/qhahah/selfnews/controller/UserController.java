package com.qhahah.selfnews.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qhahah.selfnews.utils.TwitterApiInstance;
import com.twitter.clientlib.ApiException;
import com.twitter.clientlib.api.UsersApi;
import com.twitter.clientlib.model.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RequestMapping("/2/users")
@RestController()
@Log4j2
public class UserController {
  @Autowired TwitterApiInstance twitterApiInstance;

  @GetMapping("/test")
  public String test() {
    return "test";
  }

  @GetMapping("/me")
  public String getMyUser() {
    // Set the params values
    Set<String> userFields =
        new HashSet<>(List.of()); // Set<String> | A comma separated list of User fields to display.
    Set<String> expansions =
        new HashSet<>(List.of()); // Set<String> | A comma separated list of fields to expand.
    Set<String> tweetFields =
        new HashSet<>(
            List.of()); // Set<String> | A comma separated list of Tweet fields to display.

    Get2UsersMeResponse response = null;
    try {

      UsersApi.APIfindMyUserRequest apIfindMyUserRequest =
          TwitterApiInstance.apiInstance
              .users()
              .findMyUser()
              .userFields(userFields)
              .expansions(expansions)
              .tweetFields(tweetFields);
      response = apIfindMyUserRequest.execute();
      log.info("get My User");
    } catch (ApiException e) {
      log.error(
          "Status code: {}, Reason: {}, Response Headers: {}",
          e.getCode(),
          e.getResponseBody(),
          e.getResponseHeaders());
      e.printStackTrace();
    }
    assert response != null;
    return response.toString();
  }

//   http://127.0.0.1:8080/2/users/by/username/stephenzhang233/likes
  @GetMapping("/by/username/{username}/likes")
  public String getLikesByUserName(@PathVariable("username") String userName){
      String userString = getUserByUserName(userName);
      ObjectMapper mapper = new ObjectMapper();
    try {
      User user = mapper.readValue(userString, User.class);

      String id = user.getId();
      Integer maxResults = 30; // Integer | The maximum number of results. The `max_results` query parameter value [200] is not between 5 and 100"
      String paginationToken ; // String | This parameter is used to get the next 'page' of results.
      Set<String> tweetFields = new HashSet<>(List.of()); // Set<String> | A comma separated list of Tweet fields to display.
      Set<String> expansions = new HashSet<>(List.of()); // Set<String> | A comma separated list of fields to expand.
      Set<String> mediaFields = new HashSet<>(List.of()); // Set<String> | A comma separated list of Media fields to display.
      Set<String> pollFields = new HashSet<>(List.of()); // Set<String> | A comma separated list of Poll fields to display.
      Set<String> userFields = new HashSet<>(List.of()); // Set<String> | A comma separated list of User fields to display.
      Set<String> placeFields = new HashSet<>(List.of()); // Set<String> | A comma separated list of Place fields to display.
      Get2UsersIdLikedTweetsResponse response = TwitterApiInstance.apiInstance.tweets().usersIdLikedTweets(id)
              .maxResults(maxResults)
              .tweetFields(tweetFields)
              .expansions(expansions)
              .mediaFields(mediaFields)
              .pollFields(pollFields)
              .userFields(userFields)
              .placeFields(placeFields)
              .execute();

      List<Problem> errors = response.getErrors();
      if (errors!=null&&errors.size()>0){
        errors.forEach(
          e -> {
            log.error("Error: {}", e.toString());
            if (e instanceof ResourceUnauthorizedProblem) {
              log.error(e.getTitle() + " " + e.getDetail());
            }
          });
      }

      List<Tweet> likedTweets = new ArrayList<>();
      int requestTimes = 1;
      Get2ListsIdFollowersResponseMeta get2ListsIdFollowersResponseMeta = response.getMeta();
      while (get2ListsIdFollowersResponseMeta != null){

        long startTime = System.nanoTime();
//        likedTweets.addAll(Objects.requireNonNull(response.getData()));
        Objects.requireNonNull(response.getData()).forEach(data -> {
          try {
            likedTweets.add(Tweet.fromJson(data.getText()));
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });
        log.debug(likedTweets.toString());
        paginationToken = get2ListsIdFollowersResponseMeta.getNextToken();
        response = TwitterApiInstance.apiInstance.tweets().usersIdLikedTweets(id)
                .maxResults(maxResults)
                .tweetFields(tweetFields)
                .paginationToken(paginationToken)
                .expansions(expansions)
                .mediaFields(mediaFields)
                .pollFields(pollFields)
                .userFields(userFields)
                .placeFields(placeFields)
                .execute();
        get2ListsIdFollowersResponseMeta = response.getMeta();
        TimeUnit.SECONDS.sleep(10);
        log.debug("sleep one second, request times: {}, array size: {}", ++requestTimes, likedTweets.size());
        long endTime = System.nanoTime();
        log.info("request time: {}", endTime-startTime);
      }

      return likedTweets.toString();
    } catch (Exception e) {
      e.printStackTrace();
      log.error("error: {}", e.toString());
    }
    return "";
  }
  //  Example:
  //  curl http://127.0.0.1:8080/2/users/by/username/stephenzhang233
  // {"id":"726276481811279872","name":"Stephen Zhang","username":"stephenzhang233"}
  @GetMapping("/by/username/{username}")
  public String getUserByUserName(@PathVariable("username") String userName) {
    // Set the params values
    Set<String> userFields =
        new HashSet<>(List.of()); // Set<String> | A comma separated list of User fields to display.
    Set<String> expansions =
        new HashSet<>(List.of()); // Set<String> | A comma separated list of fields to expand.
    Set<String> tweetFields =
        new HashSet<>(
            List.of()); // Set<String> | A comma separated list of Tweet fields to display.

    Get2UsersByUsernameUsernameResponse response;
    try {
      response =
          TwitterApiInstance.apiInstance
              .users()
              .findUserByUsername(userName)
              .userFields(userFields)
              .expansions(expansions)
              .tweetFields(tweetFields)
              .execute();
      if (response.getErrors() != null && response.getErrors().size() > 0) {
        response
            .getErrors()
            .forEach(
                e -> {
                  log.error("Error: {}", e.toString());
                  if (e instanceof ResourceUnauthorizedProblem) {
                    log.error(e.getTitle() + " " + e.getDetail());
                  }
                });
      }
      if (response.getData() != null) {
        log.info("getUserByUserName id: {}, Name: {}", response.getData().getId(), response.getData().getName());
        return response.getData().toJson();
      } else {
        return "null";
      }
    } catch (ApiException e) {
      log.error(
          "Status Code: {}, responseHeaders: {}, responseBody: {}",
          e.getCode(),
          e.getResponseHeaders(),
          e.getResponseBody());
      e.printStackTrace();
      return "Error";
    }
  }
}
