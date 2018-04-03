package curlapi.com.curlapicalling.http;


import java.io.File;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import curlapi.com.curlapicalling.http.AuthenticationParameters;
import curlapi.com.curlapicalling.http.SSLContextFactory;
import curlapi.com.curlapicalling.util.IOUtil;


/**
 * client-side interface to the back-end application.
 */
public class Api {

    private SSLContext sslContext;
    private int lastResponseCode;

    public int getLastResponseCode() {
        return lastResponseCode;
    }

    public Api(AuthenticationParameters authParams) throws Exception {

        File clientCertFile = authParams.getClientCertificate();

        sslContext = SSLContextFactory.getInstance().makeContext(clientCertFile, authParams.getClientCertificatePassword(), authParams.getCaCertificate());

        CookieHandler.setDefault(new CookieManager());
    }


    public String doGet(String url)  throws Exception {
        String result = null;

        HttpURLConnection urlConnection = null;
        try {
            URL requestedUrl = new URL(url);
            urlConnection = (HttpURLConnection) requestedUrl.openConnection();
            if(urlConnection instanceof HttpsURLConnection) {
                ((HttpsURLConnection)urlConnection).setSSLSocketFactory(sslContext.getSocketFactory());
            }
            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(5000/*miliseconds*/);
            urlConnection.setReadTimeout(5000/*miliseconds*/);

            lastResponseCode = urlConnection.getResponseCode();
            result = IOUtil.readFully(urlConnection.getInputStream());

        } catch(Exception ex) {
            result = ex.toString();
        } finally {
            if(urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return result;
    }


    public String doPost(String url)  throws Exception {
        String result = null;

        HttpURLConnection urlConnection = null;
        try {
            URL requestedUrl = new URL(url);
            InputStream inputStream;

            urlConnection = (HttpURLConnection) requestedUrl.openConnection();
            if(urlConnection instanceof HttpsURLConnection) {
                ((HttpsURLConnection)urlConnection).setSSLSocketFactory(sslContext.getSocketFactory());
            }
            urlConnection.setRequestMethod("POST");
            urlConnection.setConnectTimeout(5000/*miliseconds*/);
            urlConnection.setReadTimeout(5000/*miliseconds*/);


            lastResponseCode = urlConnection.getResponseCode();
            result = IOUtil.readFully(urlConnection.getInputStream());

        } catch(Exception ex) {
            result = ex.toString();
        } finally {
            if(urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return result;
    }



}
