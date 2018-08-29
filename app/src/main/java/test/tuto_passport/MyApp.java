package test.tuto_passport;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.squareup.leakcanary.LeakCanary;

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        /*Stetho - библиотека с открытым исходным кодом, разработанная в Фейсбуке.
        Предназначена для быстрой отладки приложения. Благодаря библиотеке, приложение выглядит как веб-сайт.
        С помощью инструмента Chrome DevTools вы можете легко просмотреть иерархию приложения, отслеживать сетевую активность,
        управлять базой данных SQLite, мониторить общие настройки SharedPreferences и т.д.*/
        //Для минимальной инициализации достаточно вызвать метод Stetho.initializeWithDefaults(this)
        Stetho.initializeWithDefaults(this);

//        if (LeakCanary.isInAnalyzerProcess(this)) {
//            // This process is dedicated to LeakCanary for heap analysis.
//            // You should not init your app in this process.
//            return;
//        }
//        LeakCanary.install(this);
        // Normal app init code...
    }
}
