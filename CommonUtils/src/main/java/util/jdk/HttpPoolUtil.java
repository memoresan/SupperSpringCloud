package util.jdk;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonObject;
import org.apache.http.*;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.*;
import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class HttpPoolUtil {

    private static final int CONNECT_TIMEOUT = 15000;// 设置连接建立的超时时间为10s Config.getHttpConnectTimeout()

    private static final int SOCKET_TIMEOUT = 15000;// Config.getHttpSocketTimeout();

    private static final int MAX_CONN = 1000; // 最大连接数Config.getHttpMaxPoolSize()

    private static final int Max_PRE_ROUTE = 20;// Config.getHttpMaxPoolSize()

    private static final int MAX_ROUTE = 20;// Config.getHttpMaxPoolSize();

    private static volatile CloseableHttpClient httpClient; // 发送请求的客户端单例

    private static PoolingHttpClientConnectionManager manager; // 连接池管理类

    private static ScheduledExecutorService monitorExecutor;

    private final static Object syncLock = new Object(); // 相当于线程锁,用于线程安全

    /**
     * 对http请求进行基本设置
     *
     * @param httpRequestBase http请求
     */
    private static void setRequestConfig(HttpRequestBase httpRequestBase)
    {
        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(
                CONNECT_TIMEOUT).setConnectTimeout(CONNECT_TIMEOUT).setSocketTimeout(
                SOCKET_TIMEOUT).build();
        httpRequestBase.setConfig(requestConfig);
    }







    /**
     * 获取httpclient 单例模式
     *
     * @param url
     * @return
     */
    public static CloseableHttpClient getHttpClient(String url) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {


        int port = 80;
      /*  if (url.contains(":"))
        {
            port = Integer.parseInt(args[1]);
        }*/
        if (httpClient == null)
        {
            // 多线程下多个线程同时调用getHttpClient容易导致重复创建httpClient对象的问题,所以加上了同步锁
            synchronized (syncLock)
            {
                if (httpClient == null)
                {
                    httpClient = createHttpClient(url, port);
                    // 开启监控线程,对异常和空闲线程进行关闭
                    monitorExecutor = new ScheduledThreadPoolExecutor(1);
                    monitorExecutor.scheduleAtFixedRate(HttpPoolUtil::closeIdelThread, 1, 5, TimeUnit.MILLISECONDS);
                }
            }
        }
        return httpClient;
    }

    //对空闲的线程进行关闭
    private static void closeIdelThread(){
        // 关闭异常连接
        manager.closeExpiredConnections();
        // 关闭5s空闲的连接
        manager.closeIdleConnections(5, TimeUnit.MILLISECONDS);
    }

    /**
     * 根据host和port构建httpclient实例
     *
     * @param host 要访问的域名
     * @param port 要访问的端口
     * @return
     */
    public static CloseableHttpClient createHttpClient(String host, int port) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
        SSLContextBuilder builder =  new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustStrategy() {
            @Override
            public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                return true;
            }
        });
        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(builder.build(), new String[]{"SSLv2Hello", "SSLv3", "TLSv1", "TLSv1.2"}, null, NoopHostnameVerifier.INSTANCE);
        ConnectionSocketFactory plainSocketFactory = PlainConnectionSocketFactory.getSocketFactory();
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory> create().register(
                "http", plainSocketFactory).register("https", sslSocketFactory).build();

        manager = new PoolingHttpClientConnectionManager(registry);
        // 设置连接参数
        manager.setMaxTotal(MAX_CONN); // 最大连接数
        manager.setDefaultMaxPerRoute(Max_PRE_ROUTE); // 路由最大连接数
        manager.setDefaultMaxPerRoute(Max_PRE_ROUTE);
        // 请求失败时,进行请求重试
        HttpRequestRetryHandler handler = new HttpRequestRetryHandler()
        {
            @Override
            public boolean retryRequest(IOException e, int i, HttpContext httpContext)
            {
                if (i > 3)
                {
                    // 重试超过3次,放弃请求
                    // logger.error("retry has more than 3 time, give up request");
                    return false;
                }
                if (e instanceof NoHttpResponseException)
                {
                    // 服务器没有响应,可能是服务器断开了连接,应该重试
                    return true;
                }
                if (e instanceof SSLHandshakeException)
                {
                    // SSL握手异常
                    return false;
                }
                if (e instanceof InterruptedIOException)
                {
                    // 超时
                    return false;
                }
                if (e instanceof UnknownHostException)
                {
                    // 服务器不可达
                    return false;
                }
                if (e instanceof ConnectTimeoutException)
                {
                    // 连接超时
                    return false;
                }
                if (e instanceof SSLException)
                {

                    return false;
                }

                HttpClientContext context = HttpClientContext.adapt(httpContext);
                HttpRequest request = context.getRequest();
                if (!(request instanceof HttpEntityEnclosingRequest))
                {
                    // 如果请求不是关闭连接的请求
                    return true;
                }
                return false;
            }
        };
        if(host.contains("https://")){
            return sslClient();
        }
        HttpHost httpHost ;
        if(port == 80){
            httpHost = new HttpHost(host);
        }else {
            httpHost = new HttpHost(host, port);
        }
        manager.setMaxPerRoute(new HttpRoute(httpHost), MAX_ROUTE);
        CloseableHttpClient client = HttpClients.custom().setConnectionManager(
                manager).setRetryHandler(handler).build();
        return client;
    }

   private static CloseableHttpClient sslClient() throws KeyManagementException, NoSuchAlgorithmException {
       X509TrustManager trustManager = new X509TrustManager() {
           @Override
           public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

           }

           @Override
           public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

           }

           @Override
           public X509Certificate[] getAcceptedIssuers() {
               return null;
           }
       };

       SSLContext ctx = SSLContext.getInstance(SSLConnectionSocketFactory.TLS);
       ctx.init(null, new TrustManager[] { trustManager }, null);
       SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(ctx, NoopHostnameVerifier.INSTANCE);
       RequestConfig requestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD_STRICT)
               .setExpectContinueEnabled(Boolean.TRUE)
               .setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM, AuthSchemes.DIGEST))
               .setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC)).build();
       CloseableHttpClient closeableHttpClient = HttpClients.custom().setConnectionManager(manager)
               .setSSLSocketFactory(socketFactory)
               .setDefaultRequestConfig(requestConfig).build();
       return closeableHttpClient;
   }





        /**
         * 设置post请求的参数
         *
         * @param httpPost
         * @param params 应该是表单的参数
         */
    private static void setPostParams(HttpPost httpPost, Map<String, String> params)
    {
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        Set<String> keys = params.keySet();
        for (String key : keys)
        {
            nvps.add(new BasicNameValuePair(key, params.get(key)));
        }
        try
        {
            httpPost.setEntity(new UrlEncodedFormEntity(nvps, "utf-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
    }




    /**
     * post 方式
     *
     * @param url 地址
     * @param params 参数
     * @return
     */
    public static JsonObject formPost(String url, Map<String, String> params)
    {
        HttpPost httpPost = new HttpPost(url);
        setRequestConfig(httpPost);
        setPostParams(httpPost, params);
        CloseableHttpResponse response = null;
        HttpEntity entity = null;
        try
        {
            response = getHttpClient(url).execute(httpPost, HttpClientContext.create());
            if(response.getStatusLine().getStatusCode() == 200){
                entity =  response.getEntity();
                if (entity != null)
                {
                    EntityUtils.toString(entity);
                }
            }else{
                System.out.println("失败");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                EntityUtils.consume(entity);
                if (response != null) response.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * post 方式
     *
     * @param url 地址
     * @param header  header参数
     * @param json json字符串
     * @return
     */
    public static String jsonPost(String url, Map<String, String> header,String json)
    {
        if(url.contains(" ")){
            url = url.replaceAll(" ","%20");
        }
        HttpPost httpPost = new HttpPost(url);
        setRequestConfig(httpPost);
        setPostHeader(httpPost, header);
        JSONObject jsonObject = JSONObject.parseObject(json);
        StringEntity stringEntity = new StringEntity(jsonObject.toJSONString() ,"utf-8");
        stringEntity.setContentType("application/json");
        httpPost.setEntity(stringEntity);
        CloseableHttpResponse response = null;
        HttpEntity entity = null;
        try
        {
            response = getHttpClient(url).execute(httpPost);
            if(response.getStatusLine().getStatusCode() == 200){

                entity = response.getEntity();
                if (entity != null)
                {
                    String entityStr = EntityUtils.toString(entity);
                    System.out.println("返回成功:"+entityStr);
                   return entityStr;
                }
            }else{
                System.out.println("失败:"+response);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                EntityUtils.consume(entity);
                if (response != null) response.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 设置header
     * @param httpPost
     * @param header
     */
    private static void setPostHeader(HttpPost httpPost, Map<String, String> header) {
        Set<String> keys = header.keySet();
        for (String str : keys) {
            httpPost.addHeader(str, header.get(str));
        }
    }


    private static void setGetHeader(HttpGet httpGet,Map<String, String> header){
        Set<String> keys = header.keySet();
        for (String str : keys) {
            httpGet.addHeader(str, header.get(str));
        }
    }


    /**
     * post 方式
     *
     * @param url 地址
     * @param header  header参数
     * @param file json字符串
     * @return
     */
    public static JsonObject filePost(String url, Map<String, String> header, File file)
    {
        HttpPost httpPost = new HttpPost(url);
        setRequestConfig(httpPost);
        setPostHeader(httpPost, header);
        FileBody fileBody = new FileBody(file);
        HttpEntity httpEntity = MultipartEntityBuilder.create().addPart("file",fileBody).addTextBody("module","123").build();
        httpPost.setEntity(httpEntity);
        CloseableHttpResponse response = null;
        HttpEntity entity = null;
        try
        {
            response = getHttpClient(url).execute(httpPost, HttpClientContext.create());
            if(response.getStatusLine().getStatusCode() == 200){
                entity = response.getEntity();
                if (entity != null)
                {
                    System.out.println(EntityUtils.toString(entity));
                }
            }else{
                System.out.println("失败");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                EntityUtils.consume(entity);
                if (response != null) response.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        return null;
    }


    /**
     * get 方式
     *
     * @param url 地址
     * @param param  header参数
     * @return
     */
    public static String get(String url ,Map<String, Object> param,Map<String,String> header) throws IOException {
        if(url.contains(" ")){
            url = url.replaceAll(" ","%20");
        }
        HttpGet httpGet;
        if(param != null && !param.isEmpty()){
            UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(createParam(param), Consts.UTF_8);
            String p = EntityUtils.toString(urlEncodedFormEntity);
             httpGet = new HttpGet(url+"?"+p);
        }else {
             httpGet = new HttpGet(url);

        }
        setRequestConfig(httpGet);
        setGetHeader(httpGet,header);
        CloseableHttpResponse response = null;
        HttpEntity entity = null;
        try
        {
            response = getHttpClient(url).execute(httpGet, HttpClientContext.create());
            if(response.getStatusLine().getStatusCode() == 200){
                entity = response.getEntity();
                if (entity != null)
                {
                    String entityStr = EntityUtils.toString(entity);
                    System.out.println("返回成功:"+url);
                    System.out.println("返回成功:"+entityStr);
                    return entityStr;
                }
            }else{
                System.out.println("失败:"+response+"url:"+url);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                EntityUtils.consume(entity);
                if (response != null) response.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 根据表单创建出nameValuePair,对于post和get表单的nameValue都是一样的只是最终处理不一样，
     * get是放到url中，post不是放到body中
     * @param param
     * @return
     */
    public static List<NameValuePair> createParam(Map<String, Object> param){
        Set<String> paramStr = param.keySet();
        List<NameValuePair> nameValuePairs = new ArrayList<>();
        Objects.requireNonNull(param,"param is null");
        for(String str:paramStr){
            nameValuePairs.add(new BasicNameValuePair(str,param.get(str).toString()));
        }
        return nameValuePairs;
    }




    /**
     * 关闭连接池
     */
    public static void closeConnectionPool()
    {
        try
        {
            if(httpClient != null){
                httpClient.close();
            }
            if(manager !=null){
                manager.close();
            }
            if(monitorExecutor !=null){
                monitorExecutor.shutdown();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}