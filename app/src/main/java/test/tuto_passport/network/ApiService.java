package test.tuto_passport.network;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import test.tuto_passport.entities.AccessToken;
import test.tuto_passport.entities.PostResponse;

//Апи запросов для ретрофита
public interface ApiService {

    //пост запрос на регистрацию
    @POST("register")
    //@FormUrlEncoded-Обозначает, что тело запроса будет использовать форму URL-кодирования.
    // Имена полей и значения будут кодироваться в кодировке UTF-8 до кодирования URI в соответствии с RFC-3986 .
    @FormUrlEncoded
    Call<AccessToken> register(@Field("name") String name, @Field("email") String email, @Field("password") String password);

    //Пост запрос на логин и пароль(проверку).
    @POST("login")
    @FormUrlEncoded
    Call<AccessToken> login(@Field("username") String username, @Field("password") String password);

    //походу не очень понятно, но скорее всего запрос на регестрированого пользователя
    @POST("social_auth")
    @FormUrlEncoded
    Call<AccessToken> socialAuth(@Field("name") String name,
                                 @Field("email") String email,
                                 @Field("provider") String provider,
                                 @Field("provider_user_id") String providerUserId);

    //Запрос на рефреш токен, для смены аксес токена
    @POST("refresh")
    @FormUrlEncoded
    Call<AccessToken> refresh(@Field("refresh_token") String refreshToken);

    @GET("posts")
    Call<PostResponse> posts();

}
