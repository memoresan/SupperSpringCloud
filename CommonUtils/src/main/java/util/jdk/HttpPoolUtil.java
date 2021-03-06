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

    private static final int CONNECT_TIMEOUT = 15000;// ????????????????????????????????????10s Config.getHttpConnectTimeout()

    private static final int SOCKET_TIMEOUT = 15000;// Config.getHttpSocketTimeout();

    private static final int MAX_CONN = 1000; // ???????????????Config.getHttpMaxPoolSize()

    private static final int Max_PRE_ROUTE = 20;// Config.getHttpMaxPoolSize()

    private static final int MAX_ROUTE = 20;// Config.getHttpMaxPoolSize();

    private static volatile CloseableHttpClient httpClient; // ??????????????????????????????

    private static PoolingHttpClientConnectionManager manager; // ??????????????????

    private static ScheduledExecutorService monitorExecutor;

    private final static Object syncLock = new Object(); // ??????????????????,??????????????????

    /**
     * ???http????????????????????????
     *
     * @param httpRequestBase http??????
     */
    private static void setRequestConfig(HttpRequestBase httpRequestBase)
    {
        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(
                CONNECT_TIMEOUT).setConnectTimeout(CONNECT_TIMEOUT).setSocketTimeout(
                SOCKET_TIMEOUT).build();
        httpRequestBase.setConfig(requestConfig);
    }







    /**
     * ??????httpclient ????????????
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
            // ????????????????????????????????????getHttpClient????????????????????????httpClient???????????????,????????????????????????
            synchronized (syncLock)
            {
                if (httpClient == null)
                {
                    httpClient = createHttpClient(url, port);
                    // ??????????????????,????????????????????????????????????
                    monitorExecutor = new ScheduledThreadPoolExecutor(1);
                    monitorExecutor.scheduleAtFixedRate(HttpPoolUtil::closeIdelThread, 1, 5, TimeUnit.MILLISECONDS);
                }
            }
        }
        return httpClient;
    }

    //??????????????????????????????
    private static void closeIdelThread(){
        // ??????????????????
        manager.closeExpiredConnections();
        // ??????5s???????????????
        manager.closeIdleConnections(5, TimeUnit.MILLISECONDS);
    }

    /**
     * ??????host???port??????httpclient??????
     *
     * @param host ??????????????????
     * @param port ??????????????????
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
        // ??????????????????
        manager.setMaxTotal(MAX_CONN); // ???????????????
        manager.setDefaultMaxPerRoute(Max_PRE_ROUTE); // ?????????????????????
        manager.setDefaultMaxPerRoute(Max_PRE_ROUTE);
        // ???????????????,??????????????????
        HttpRequestRetryHandler handler = new HttpRequestRetryHandler()
        {
            @Override
            public boolean retryRequest(IOException e, int i, HttpContext httpContext)
            {
                if (i > 3)
                {
                    // ????????????3???,????????????
                    // logger.error("retry has more than 3 time, give up request");
                    return false;
                }
                if (e instanceof NoHttpResponseException)
                {
                    // ?????????????????????,?????????????????????????????????,????????????
                    return true;
                }
                if (e instanceof SSLHandshakeException)
                {
                    // SSL????????????
                    return false;
                }
                if (e instanceof InterruptedIOException)
                {
                    // ??????
                    return false;
                }
                if (e instanceof UnknownHostException)
                {
                    // ??????????????????
                    return false;
                }
                if (e instanceof ConnectTimeoutException)
                {
                    // ????????????
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
                    // ???????????????????????????????????????
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
         * ??????post???????????????
         *
         * @param httpPost
         * @param params ????????????????????????
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
     * post ??????
     *
     * @param url ??????
     * @param params ??????
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
                System.out.println("??????");
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
     * post ??????
     *
     * @param url ??????
     * @param header  header??????
     * @param json json?????????
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
                    System.out.println("????????????:"+entityStr);
                   return entityStr;
                }
            }else{
                System.out.println("??????:"+response);
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
     * ??????header
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
     * post ??????
     *
     * @param url ??????
     * @param header  header??????
     * @param file json?????????
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
                System.out.println("??????");
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
     * get ??????
     *
     * @param url ??????
     * @param param  header??????
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
                    System.out.println("????????????:"+url);
                    System.out.println("????????????:"+entityStr);
                    return entityStr;
                }
            }else{
                System.out.println("??????:"+response+"url:"+url);
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
     * ?????????????????????nameValuePair,??????post???get?????????nameValue?????????????????????????????????????????????
     * get?????????url??????post????????????body???
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
     * ???????????????
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