package rxhttp.wrapper;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.Util;
import okhttp3.internal.cache.DiskLruCache;
import okhttp3.internal.cache.DiskLruCache.Companion;
import okhttp3.internal.concurrent.TaskRunner;
import okhttp3.internal.http.StatusLine;
import okhttp3.internal.io.FileSystem;
import rxhttp.wrapper.annotations.Nullable;
import rxhttp.wrapper.callback.IConverter;
import rxhttp.wrapper.entity.DownloadOffSize;
import rxhttp.wrapper.param.Param;

/**
 * 此类的作用在于兼用OkHttp版本  注意: 本类一定要用Java语言编写，kotlin将无法兼容新老版本
 * User: ljx
 * Date: 2020/5/17
 * Time: 15:28
 */
public class OkHttpCompat {

    private static String OKHTTP_USER_AGENT;

    public static IConverter getConverter(Response response) {
        return response.request().tag(IConverter.class);
    }

    @Nullable
    public static DownloadOffSize getDownloadOffSize(Response response) {
        return response.request().tag(DownloadOffSize.class);
    }

    public static boolean needDecodeResult(Response response) {
        return !"false".equals(response.request().header(Param.DATA_DECRYPT));
    }

    public static void closeQuietly(Response response) {
        if (response == null) return;
        closeQuietly(response.body());
    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable == null) return;
        Util.closeQuietly(closeable);
    }

    public static Request request(Response response) {
        return response.request();
    }

    public static List<String> pathSegments(Response response) {
        return response.request().url().pathSegments();
    }

    public static HttpUrl url(Request request) {
        return request.url();
    }

    public static Headers headers(Response response) {
        return response.headers();
    }

    public static ResponseBody requireBody(Response response) {
        ResponseBody body = response.body();
        return Objects.requireNonNull(body, "response with no body");
    }

    public static String header(Response response, String name) {
        return response.header(name);
    }

    public static long receivedResponseAtMillis(Response response) {
        return response.receivedResponseAtMillis();
    }

    //从响应头 Content-Range 中，取 contentLength
    public static long getContentLength(Response response) {
        long contentLength = -1;
        ResponseBody body = response.body();
        if (body != null) {
            if ((contentLength = body.contentLength()) != -1) {
                return contentLength;
            }
        }
        String headerValue = response.header("Content-Range");
        if (headerValue != null) {
            //响应头Content-Range格式 : bytes 100001-20000000/20000001
            try {
                int divideIndex = headerValue.indexOf("/"); //斜杠下标
                int blankIndex = headerValue.indexOf(" ");
                String fromToValue = headerValue.substring(blankIndex + 1, divideIndex);
                String[] split = fromToValue.split("-");
                long start = Long.parseLong(split[0]); //开始下载位置
                long end = Long.parseLong(split[1]);   //结束下载位置
                contentLength = end - start + 1;       //要下载的总长度
            } catch (Exception ignore) {
            }
        }
        return contentLength;
    }

    //解析http状态行
    public static StatusLine parse(String statusLine) throws IOException {
        String okHttpUserAgent = getOkHttpUserAgent();
        if (okHttpUserAgent.compareTo("okhttp/4.0.0") >= 0) {
            return StatusLine.Companion.parse(statusLine);
        } else {
            Class<StatusLine> statusLineClass = StatusLine.class;
            try {
                Method parse = statusLineClass.getDeclaredMethod("parse", String.class);
                return (StatusLine) parse.invoke(statusLineClass, statusLine);
            } catch (Exception e) {
                throw new IOException(e.getMessage());
            }
        }
    }

    public static DiskLruCache newDiskLruCache(FileSystem fileSystem, File directory, int appVersion, int valueCount, long maxSize) {
        String okHttpVersion = getOkHttpUserAgent();
        if (okHttpVersion.compareTo("okhttp/4.3.0") >= 0) {
            return new DiskLruCache(fileSystem, directory, appVersion, valueCount, maxSize, TaskRunner.INSTANCE);
        } else if (okHttpVersion.compareTo("okhttp/4.0.0") >= 0) {
            Companion companion = DiskLruCache.Companion;
            Class<? extends Companion> clazz = companion.getClass();
            try {
                Method create = clazz.getDeclaredMethod("create", FileSystem.class, File.class, int.class, int.class, long.class);
                return (DiskLruCache) create.invoke(companion, fileSystem, directory, appVersion, valueCount, maxSize);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } else {
            Class<DiskLruCache> clazz = DiskLruCache.class;
            try {
                Method create = clazz.getDeclaredMethod("create", FileSystem.class, File.class, int.class, int.class, long.class);
                return (DiskLruCache) create.invoke(null, fileSystem, directory, appVersion, valueCount, maxSize);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        throw new RuntimeException("Please upgrade OkHttp to V3.12.0 or higher");
    }

    //获取OkHttp版本号
    public static String getOkHttpUserAgent() {
        if (OKHTTP_USER_AGENT != null) return OKHTTP_USER_AGENT;
        try {
            //4.7.x及以上版本获取userAgent方式
            Class<?> clazz = Class.forName("okhttp3.internal.Util");
            return OKHTTP_USER_AGENT = (String) clazz.getDeclaredField("userAgent").get(null);
        } catch (Throwable ignore) {
        }
        try {
            Class<?> clazz = Class.forName("okhttp3.internal.Version");
            try {
                //4.x.x及以上版本获取userAgent方式
                Field userAgent = clazz.getDeclaredField("userAgent");
                return OKHTTP_USER_AGENT = (String) userAgent.get(null);
            } catch (Exception ignore) {
            }
            //4.x.x以下版本获取userAgent方式
            Method userAgent = clazz.getDeclaredMethod("userAgent");
            return OKHTTP_USER_AGENT = (String) userAgent.invoke(null);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return OKHTTP_USER_AGENT = "okhttp/4.2.0";
    }
}
