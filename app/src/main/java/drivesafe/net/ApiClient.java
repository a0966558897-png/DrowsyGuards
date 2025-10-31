package drivesafe.net;

import java.util.concurrent.TimeUnit;

import drivesafe.db.TokenStore;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/** 建立並回傳 Retrofit；呼叫端用 .create(ApiService.class) 取得介面 */
public class ApiClient {

    private static Retrofit retrofit;
    private static String lastBaseUrl;

    private static Retrofit authedRetrofit;
    private static String lastAuthedBaseUrl;

    public static Retrofit get(String baseUrl) {
        if (retrofit == null || lastBaseUrl == null || !lastBaseUrl.equals(baseUrl)) {
            retrofit = buildRetrofit(baseUrl, buildOkHttp(false, null));
            lastBaseUrl = baseUrl;
        }
        return retrofit;
    }

    public static Retrofit getAuthed(String baseUrl, final TokenStore tokenStore) {
        if (authedRetrofit == null || lastAuthedBaseUrl == null || !lastAuthedBaseUrl.equals(baseUrl)) {
            authedRetrofit = buildRetrofit(baseUrl, buildOkHttp(true, tokenStore));
            lastAuthedBaseUrl = baseUrl;
        }
        return authedRetrofit;
    }

    public static <T> T create(String baseUrl, Class<T> service) {
        return get(baseUrl).create(service);
    }

    public static <T> T createAuthed(String baseUrl, TokenStore tokenStore, Class<T> service) {
        return getAuthed(baseUrl, tokenStore).create(service);
    }

    private static Retrofit buildRetrofit(String baseUrl, OkHttpClient ok) {
        return new Retrofit.Builder()
                .baseUrl(ensureEndsWithSlash(baseUrl))
                .addConverterFactory(GsonConverterFactory.create())
                .client(ok)
                .build();
    }

    private static OkHttpClient buildOkHttp(boolean withAuth, final TokenStore tokenStore) {
        HttpLoggingInterceptor log = new HttpLoggingInterceptor();
        log.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder b = new OkHttpClient.Builder()
                .addInterceptor(log)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS);

        if (withAuth) {
            Interceptor auth = chain -> {
                Request orig = chain.request();
                String token = tokenStore == null ? null : tokenStore.get();
                Request.Builder rb = orig.newBuilder();
                if (token != null && !token.isEmpty()) {
                    rb.addHeader("Authorization", "Bearer " + token);
                }
                return chain.proceed(rb.build());
            };
            b.addInterceptor(auth);
        }
        return b.build();
    }

    private static String ensureEndsWithSlash(String url) {
        return url.endsWith("/") ? url : (url + "/");
    }
}
