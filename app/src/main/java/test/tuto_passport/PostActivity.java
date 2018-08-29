package test.tuto_passport;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import test.tuto_passport.entities.PostResponse;
import test.tuto_passport.network.ApiService;
import test.tuto_passport.network.RetrofitBuilder;

public class PostActivity extends AppCompatActivity {

    private static final String TAG = "PostActivity";

    // @BindView - анотация для  ButterKnife
    @BindView(R.id.post_title)
    TextView title;

    ApiService service;
    TokenManager tokenManager;
    Call<PostResponse> call;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        //Либа которая сокращает написание однотипноьго кода. В данный момент ето View елементы.
        ButterKnife.bind(this);

        //Получаем токен с уже существующими значениями(рефреш токен и аксес токен)
        tokenManager = TokenManager.getInstance(getSharedPreferences("prefs", MODE_PRIVATE));

        //Если токен пустой, тогда регестрируемся.
        if(tokenManager.getToken() == null){
            startActivity(new Intent(PostActivity.this, LoginActivity.class));
            finish();
        }

        //Создаем сервис на основе нашего апи и токена
        service = RetrofitBuilder.createServiceWithAuth(ApiService.class, tokenManager);
    }

    //Если нажимаем кнопку Posts, тогда выполняется запрос post
    //Заливаем данные в title при успешном выполнении. Если нет - удаляем токен
    //Переходим на логин активити.
    @OnClick(R.id.btn_posts)
    void getPosts(){

        call = service.posts();
        call.enqueue(new Callback<PostResponse>() {
            @Override
            public void onResponse(Call<PostResponse> call, Response<PostResponse> response) {
                Log.w(TAG, "onResponse: " + response );

                if(response.isSuccessful()){
                    title.setText(response.body().getData().get(0).getTitle());
                }else {
                    tokenManager.deleteToken();
                    startActivity(new Intent(PostActivity.this, LoginActivity.class));
                    finish();

                }
            }

            @Override
            public void onFailure(Call<PostResponse> call, Throwable t) {
                Log.w(TAG, "onFailure: " + t.getMessage() );
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(call != null){
            call.cancel();
            call = null;
        }
    }
}
