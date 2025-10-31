package drivesafe.net;

import com.example.drivesafe.db.ApiService;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static volatile ApiService API;
    private static volatile String LAST_BASE;

    public static ApiService api(String baseUrl) {
        if (API == null || LAST_BASE == null || !LAST_BASE.equals(baseUrl)) {
            synchronized (RetrofitClient.class) {
                if (API == null || LAST_BASE == null || !LAST_BASE.equals(baseUrl)) {
                    Retrofit r = new Retrofit.Builder()
                            .baseUrl(ensureSlash(baseUrl))
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                    API = r.create(ApiService.class);
                    LAST_BASE = baseUrl;
                }
            }
        }
        return API;
    }

    private static String ensureSlash(String url) {
        return url.endsWith("/") ? url : (url + "/");
    }
}
