package test.tuto_passport.network;

import com.facebook.stetho.okhttp3.StethoInterceptor;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;
import test.tuto_passport.BuildConfig;
import test.tuto_passport.TokenManager;

// стандартный билдер для ретрофита
public class RetrofitBuilder {

    //адрес обращения
    private static final String BASE_URL = "http://192.168.0.10/tutos/tuto_passport/public/api/";

    private final static OkHttpClient client = buildClient();
    private final static Retrofit retrofit = buildRetrofit(client);

    /* Интерцепторы — это мощный способ кастомизации запросами в Retrofit. Эта возможность была полезна в Retrofit 1,
     и будет полезной в Retrofit 2. Популярный способ использования — когда вы хотите перехватить запрос и, к примеру, добавить свои заголовки.
     К примеру, вы хотите передать авторизационный токен в заголовоке Authorization.Так как Retrofit глубоко интегрирован с OkHttp, вам будет нужно
     кастомизировать OkHttpClient и добавить Interceptor. В следующем примере мы перехватим запрос и добавим заголовки Accept и Connection.
     Если вы используете кастомный OkHttpClient, вам будет нужно задать клиент в Retrofit.Builder, используя метод client().
     Таким образом вы замените стандартный клиент своей дополненной версией.Вы можете использовать интерцепторы для различных задач, таких как аутентификация,
     логгирование, для различных манипуляций с запросами и ответами, и т.д */
    private static OkHttpClient buildClient(){
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request();

                        Request.Builder builder = request.newBuilder()
                                .addHeader("Accept", "application/json")
                                .addHeader("Connection", "close");

                        request = builder.build();

                        return chain.proceed(request);

                    }
                });

        if(BuildConfig.DEBUG){
            builder.addNetworkInterceptor(new StethoInterceptor());
        }

        return builder.build();

    }

    //Создаем ретрофит билдер и подключаем в клиенте кастомизированый client http
    private static Retrofit buildRetrofit(OkHttpClient client){
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create())
                .build();
    }

    //Метод для передачи модели апи в билдер ретроофит
    // <T> - возвращаемый объект любого типа, но только одного после инициализации.
    public static <T> T createService(Class<T> service){
        return retrofit.create(service);
    }

    //Класс создания сервиса аутентивикации
    //Так же использует OkhttpClient и перехватчик, используя client который основывается на изменненом запросе
    public static <T> T createServiceWithAuth(Class<T> service, final TokenManager tokenManager){

        OkHttpClient newClient = client.newBuilder().addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {

                Request request = chain.request();

                Request.Builder builder = request.newBuilder();

                //Если аксес токен не null, то мы его добавляем в запрос
                if(tokenManager.getToken().getAccessToken() != null){
                    builder.addHeader("Authorization", "Bearer " + tokenManager.getToken().getAccessToken());
                }
                request = builder.build();
                return chain.proceed(request);
            }
            //Отвечает на вызов аутентификации с удаленного веб-сервера или прокси-сервера.
            // Реализации могут либо попытаться удовлетворить проблему, возвращая запрос,
            // который включает заголовок авторизации, либо они могут отказаться от вызова путем возврата null.
            // В этом случае неаутентифицированный ответ будет возвращен вызывающему абоненту, который его вызвал.
        }).authenticator(CustomAuthenticator.getInstance(tokenManager)).build();

        //Создает новый билдер ретрофит на основе етго okhttpclient
        Retrofit newRetrofit = retrofit.newBuilder().client(newClient).build();
        return newRetrofit.create(service);

    }

    public static Retrofit getRetrofit() {
        return retrofit;
    }
}
