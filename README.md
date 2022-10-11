# java-filmorate
![ER](/assets/filmorate-ER.png)

Example queries

1. getPopularFilms

```
SELECT 
    film_likes.films_id,
    COUNT(DISTINCT film_likes.user_id) AS likes,
    films.name,
    films.description,
    films.release_date,
    films.duration,
    films.rating_mpa
FROM 
    film_likes AS film_likes
    LEFT JOIN films AS films
        ON film_likes.films_id = films.id
GROUP BY 
    film_likes.films_id,
    films.name,
    films.description,
    films.release_date,
    films.duration,
    films.rating_mpa
ORDER BY 
    COUNT(DISTINCT film_likes.user_id) DESC
LIMIT 10
```

2. getCommonFriends

```
SELECT 
    user_friends.friend_id,
    friends.email,
    friends.login,
    friends.name,
    friends.birthday
FROM 
    user_friends AS user_friends
    JOIN user_friends AS other_user_friends
        ON user_friends.friend_id = other_user_friends.friend_id
    LEFT JOIN users AS friends
        ON user_friends.friend_id = friends.id
WHERE 
    user_friends.user_id = @id
    AND other_user_friends.user_id = @other_id
```
    


