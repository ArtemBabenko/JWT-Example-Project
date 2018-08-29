package test.tuto_passport;

import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.transition.TransitionManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;

import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.POST;
import test.tuto_passport.entities.AccessToken;
import test.tuto_passport.entities.ApiError;
import test.tuto_passport.network.ApiService;
import test.tuto_passport.network.RetrofitBuilder;

import static android.content.Context.MODE_PRIVATE;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    // @BindView - анотация для  ButterKnife
    @BindView(R.id.til_email)
    TextInputLayout tilEmail;
    @BindView(R.id.til_password)
    TextInputLayout tilPassword;
    @BindView(R.id.container)
    RelativeLayout container;
    @BindView(R.id.form_container)
    LinearLayout formContainer;
    @BindView(R.id.loader)
    ProgressBar loader;

    ApiService service;
    TokenManager tokenManager;

    //Либа которая проверяет форму.Проверка формы обычно происходит на стороне сервера, где мы проверяем, находятся ли данные,
    // предоставленные пользователем или клиентом, в правильном формате или нет. AwesomeValidation Library для проверки полей ввода в форме.
    AwesomeValidation validator;
    Call<AccessToken> call;
    FacebookManager facebookManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Либа которая сокращает написание однотипноьго кода. В данный момент ето View елементы.
        ButterKnife.bind(this);

        //Создаем сервис для ретрофита, создаем файл для данных, инициализируем валидатор для проверки
        //фейсбук менеджер сразу заливаем токеном и сервисом
        service = RetrofitBuilder.createService(ApiService.class);
        tokenManager = TokenManager.getInstance(getSharedPreferences("prefs", MODE_PRIVATE));
        validator = new AwesomeValidation(ValidationStyle.TEXT_INPUT_LAYOUT);
        facebookManager = new FacebookManager(service, tokenManager);
        setupRules();

        //Если аксес токен не null тогда стартуем PostActivity
        if(tokenManager.getToken().getAccessToken() != null){
            startActivity(new Intent(LoginActivity.this, PostActivity.class));
            finish();
        }
    }

    //Метод который визиализирует загрузку с помощью TransitionManager(библиотека анимации)
    private void showLoading(){
        TransitionManager.beginDelayedTransition(container);
        formContainer.setVisibility(View.GONE);
        loader.setVisibility(View.VISIBLE);
    }

    //Метод который визиализирует отображение с помощью TransitionManager(библиотека анимации)
    private void showForm(){
        TransitionManager.beginDelayedTransition(container);
        formContainer.setVisibility(View.VISIBLE);
        loader.setVisibility(View.GONE);
    }

    //При нажатии кнопки запускается метод логирования через фейсбук!
    //Вметоде запускается метод загрузки(анимация), метод  логин с фейсбук менеджера.
    //В нем он сверяет токены,и если все гуд то стартует активити, а если нет, то
    //если нет, то отображает я так понял форму регистрация и тостом оповещает об етом
    @OnClick(R.id.btn_facebook)
    void loginFacebook(){
        showLoading();
        facebookManager.login(this, new FacebookManager.FacebookLoginListener() {
            @Override
            public void onSuccess() {
                facebookManager.clearSession();
                startActivity(new Intent(LoginActivity.this, PostActivity.class));
                finish();
            }

            @Override
            public void onError(String message) {
                showForm();
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });

    }

    //По нажатию кнопки запускает метод логирования
    //Берем даные с полей, и если проверил валидатор на соответсвие, тогда
    //делаем запрос с логином и паролем. Если все успешно - сохраняем оба токена и стартуем PostActivity.
    //Если не успешно - вылетают ошибки 401 и 422(при которой запускается метод) и отображается сразу форма.
    @OnClick(R.id.btn_login)
    void login() {

        String email = tilEmail.getEditText().getText().toString();
        String password = tilPassword.getEditText().getText().toString();

        tilEmail.setError(null);
        tilPassword.setError(null);

        validator.clear();

        if (validator.validate()) {
            showLoading();
            call = service.login(email, password);
            call.enqueue(new Callback<AccessToken>() {
                @Override
                public void onResponse(Call<AccessToken> call, Response<AccessToken> response) {

                    Log.w(TAG, "onResponse: " + response);

                    if (response.isSuccessful()) {
                        tokenManager.saveToken(response.body());
                        startActivity(new Intent(LoginActivity.this, PostActivity.class));
                        finish();
                    } else {
                        if (response.code() == 422) {
                            handleErrors(response.errorBody());
                        }
                        if (response.code() == 401) {
                            ApiError apiError = Utils.converErrors(response.errorBody());
                            Toast.makeText(LoginActivity.this, apiError.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        showForm();
                    }

                }

                @Override
                public void onFailure(Call<AccessToken> call, Throwable t) {
                    Log.w(TAG, "onFailure: " + t.getMessage());
                    showForm();
                }
            });

        }

    }

    //Кнопка регистрации перенаправляет на RegisterAvtivity
    @OnClick(R.id.go_to_register)
    void goToRegister(){
        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
    }

    //Метод который обрабатывает ошибки.
    private void handleErrors(ResponseBody response) {

        ApiError apiError = Utils.converErrors(response);

        for (Map.Entry<String, List<String>> error : apiError.getErrors().entrySet()) {
            if (error.getKey().equals("username")) {
                tilEmail.setError(error.getValue().get(0));
            }
            if (error.getKey().equals("password")) {
                tilPassword.setError(error.getValue().get(0));
            }
        }

    }

    //Настройка валидатора
    public void setupRules() {

        validator.addValidation(this, R.id.til_email, Patterns.EMAIL_ADDRESS, R.string.err_email);
        validator.addValidation(this, R.id.til_password, RegexTemplate.NOT_EMPTY, R.string.err_password);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        facebookManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (call != null) {
            call.cancel();
            call = null;
        }
        facebookManager.onDestroy();
    }
}
