package test.tuto_passport;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import test.tuto_passport.network.ApiService;

public class FacebookManager {

    private static final String TAG = "FacebookManager";
    private static final String PROVIDER = "facebook";

    //Интерфейс с двумя методами: Успех и Oшибка
    public interface FacebookLoginListener{
        void onSuccess();
        void onError(String message);
    }

    private ApiService apiService;
    private TokenManager tokenManager;
    private CallbackManager callbackManager;
    private FacebookLoginListener listener;
    private Call<test.tuto_passport.entities.AccessToken> call;

    //Класс обратного вызова для SDK для Facebook.
    private FacebookCallback<LoginResult> facebookCallback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            //заливаем аксес токен в метод запроса
            fetchUser(loginResult.getAccessToken());
        }

        @Override
        public void onCancel() {

        }

        @Override
        public void onError(FacebookException error) {
            listener.onError(error.getMessage());
        }
    };

    //Конструктор который принимает на вход апи сервис и токен
    public FacebookManager(ApiService apiService, TokenManager tokenManager){

        this.apiService = apiService;
        this.tokenManager = tokenManager;
        this.callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, facebookCallback);

    }


    public void login(Activity activity, FacebookLoginListener listener){
        this.listener = listener;

        //AccessToken(фейсбуковский) - этот класс представляет собой неизменяемый токен доступа для использования API Facebook.
        //getCurrentAccessToken() - Getter для токена доступа, который является текущим для приложения.
        //Возвращает.Токен доступа, текущий для приложения
        //Если он не null тогда пуляем его ав метод запроса пользователя
        if(AccessToken.getCurrentAccessToken() != null){
            fetchUser(AccessToken.getCurrentAccessToken());
        }else{
            LoginManager.getInstance().logInWithReadPermissions(activity, Arrays.asList("public_profile", "email"));
        }

    }

    //Метод запроса о данных пользователя на фейсбук
    private void fetchUser(AccessToken accessToken){
        // GraphRequest - запрос, который будет отправлен на платформу Facebook через Graph API.
        //Создает новый запрос, настроенный для получения собственного профиля пользователя.
        GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {

                try {
                    //Получаем данный с сапроса и заряжам в тиокен для серверной части
                    String id = object.getString("id");
                    String name = object.getString("first_name");
                    String email = object.getString("email");
                    getTokenFromBackend(name, email, PROVIDER, id);
                } catch (JSONException e) {
                    e.printStackTrace();
                    listener.onError(e.getMessage());
                }

            }
        });

        //По умолчанию метод newMeRequest извлекает из объекта пользователя поля, заданные по умолчанию.
        // Если вам нужны дополнительные поля или в целях повышения производительности нужно сократить полезную нагрузку ответа,
        // добавьте параметр fields и запросите требуемые поля.
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id, first_name, email");
        request.setParameters(parameters);
        request.executeAsync();
    }

    //Сохраняем оба токена для бекенда
    private void getTokenFromBackend(String name, String email, String provider, String providerUserId){

        //Запрос о зарегестрированом пользователе на фейс
        call = apiService.socialAuth(name, email, provider, providerUserId);
        call.enqueue(new Callback<test.tuto_passport.entities.AccessToken>() {
            @Override
            public void onResponse(Call<test.tuto_passport.entities.AccessToken> call, Response<test.tuto_passport.entities.AccessToken> response) {
                if(response.isSuccessful()){
                    //Получаем ответ, и сохраняем аксес и рефреш токены и говрим что запрос выполнен
                    tokenManager.saveToken(response.body());
                    listener.onSuccess();
                }else{
                    listener.onError("An error occured");
                }
            }

            @Override
            public void onFailure(Call<test.tuto_passport.entities.AccessToken> call, Throwable t) {
                listener.onError(t.getMessage());
            }
        });

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    //При дестрое класа подчищаем хвосты
    public void onDestroy(){
        if(call != null){
            call.cancel();
        }
        call = null;
        if(callbackManager != null){
            LoginManager.getInstance().unregisterCallback(callbackManager);
        }
    }

    public void clearSession(){
        LoginManager.getInstance().logOut();
    }
}
