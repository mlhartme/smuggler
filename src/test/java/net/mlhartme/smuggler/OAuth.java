package net.mlhartme.smuggler;

import com.sun.jersey.api.uri.UriComponent;
import com.sun.jersey.oauth.signature.*;
import net.mlhartme.smuggler.cli.Config;
import net.mlhartme.smuggler.smugmug.Account;
import net.mlhartme.smuggler.smugmug.Folder;
import net.oneandone.sushi.fs.*;
import net.oneandone.sushi.fs.http.HttpFilesystem;
import net.oneandone.sushi.fs.http.HttpNode;
import net.oneandone.sushi.fs.http.MovedTemporarilyException;
import net.oneandone.sushi.fs.http.StatusException;
import net.oneandone.sushi.fs.http.model.Request;
import net.oneandone.sushi.fs.http.model.Response;
import net.oneandone.sushi.fs.http.model.StatusCode;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

public class OAuth {

    public static void main(String[] args) throws Exception {
        World world;
        HttpNode folder;
        Config config;
        InputStream in;
        int c;

        world = World.create();
        config = Config.load(world);
        HttpFilesystem.wireLog("sushiwire.log");
        folder = (HttpNode) world.node("https://api.smugmug.com/api/v2/folder/user/mlhartme");
        in = get(folder, config);
        while (true) {
            c = in.read();
            if (c == -1) {
                break;
            }
            System.out.print((char) c);
        }
    }

    public static InputStream get(HttpNode resource, Config config) throws IOException, OAuthSignatureException {
        Request get;
        Response response;

        get = new Request("GET", resource);
        get.bodyHeader(null);
        addOauth(resource.getUri().toURL(), get, config);
        response = get.responseHeader(get.open(null));
        if (response.getStatusLine().code == StatusCode.OK) {
            return new FilterInputStream(response.getBody().content) {
                private boolean freed = false;

                @Override
                public void close() throws IOException {
                    if (!freed) {
                        freed = true;
                        get.free(response);
                    }
                    super.close();
                }
            };
        } else {
            get.free(response);
            switch (response.getStatusLine().code) {
                case StatusCode.MOVED_TEMPORARILY:
                    throw new MovedTemporarilyException(response.getHeaderList().getFirstValue("Location"));
                case StatusCode.NOT_FOUND:
                case StatusCode.GONE:
                case StatusCode.MOVED_PERMANENTLY:
                    throw new net.oneandone.sushi.fs.FileNotFoundException(resource);
                default:
                    throw new StatusException(response.getStatusLine());
            }
        }
    }


    //-- TODO
    // see also: https://oauth.net/core/1.0a/

    public static void addOauth(URL url, Request request, Config config) throws OAuthSignatureException, MalformedURLException {
        StringBuilder builder;

        builder = new StringBuilder();
        arg(builder, "OAuth oauth_nonce", UUID.randomUUID().toString());
        arg(builder, "oauth_token", config.tokenId);
        arg(builder, "oauth_consumer_key", config.consumerKey);
        arg(builder, "oauth_signature_method", "HMAC-SHA1");
        arg(builder, "oauth_version", "1.0");
        arg(builder, "oauth_timestamp", new Long(System.currentTimeMillis() / 1000).toString());
        arg(builder, "oauth_signature", signature(url, config));

        request.addRequestHeader("Authorization", builder.toString());
    }

    private static String signature(URL url, Config config) throws OAuthSignatureException {
        OAuthSignatureMethod method = new HMAC_SHA1();
        OAuthSecrets secrets;

        secrets = new OAuthSecrets().consumerSecret(config.consumerSecret);
        secrets.setTokenSecret(config.tokenSecret);
        try {
            return method.sign(elements(new OAuthRequest() {
                @Override
                public String getRequestMethod() {
                    return "GET";
                }

                @Override
                public URL getRequestURL() {
                    return url;
                }

                @Override
                public Set<String> getParameterNames() {
                    return Collections.emptySet();
                }

                @Override
                public List<String> getParameterValues(String s) {
                    return Collections.emptyList();
                }

                @Override
                public List<String> getHeaderValues(String s) {
                    return null;
                }

                @Override
                public void addHeaderValue(String s, String s1) throws IllegalStateException {

                }
            }, null), secrets);
        } catch (InvalidSecretException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void arg(StringBuilder builder, String key, String value) {
        if (builder.length() > 0) {
            builder.append(", ");
        }
        builder.append(key);
        builder.append('=');
        builder.append('"');
        builder.append(value);
        builder.append('"');
    }

    private static String elements(OAuthRequest request, OAuthParameters params) throws OAuthSignatureException {
        StringBuilder buf = new StringBuilder(request.getRequestMethod().toUpperCase());
        buf.append('&').append(UriComponent.encode(constructRequestURL(request).toASCIIString(), UriComponent.Type.UNRESERVED));
        buf.append('&').append(UriComponent.encode(normalizeParameters(request, params), UriComponent.Type.UNRESERVED));
        return buf.toString();
    }

    private static URI constructRequestURL(OAuthRequest request) throws OAuthSignatureException {
        try {
            URL mue = request.getRequestURL();
            if(mue == null) {
                throw new OAuthSignatureException();
            } else {
                StringBuffer buf = (new StringBuffer(mue.getProtocol())).append("://").append(mue.getHost().toLowerCase());
                int port = mue.getPort();
                if(port > 0 && port != mue.getDefaultPort()) {
                    buf.append(':').append(port);
                }

                buf.append(mue.getPath());
                return new URI(buf.toString());
            }
        } catch (URISyntaxException var4) {
            throw new OAuthSignatureException(var4);
        }
    }

    static String normalizeParameters(OAuthRequest request, OAuthParameters params) {
        if (params == null) {
            return ""; // TODO
        }
        ArrayList list = new ArrayList();
        Iterator buf = params.keySet().iterator();

        String i;
        while(buf.hasNext()) {
            i = (String)buf.next();
            if(!i.equals("realm") && !i.equals("oauth_signature")) {
                String param = (String)params.get(i);
                if(param != null) {
                    addParam(i, param, list);
                }
            }
        }

        buf = request.getParameterNames().iterator();

        while(true) {
            List param1;
            do {
                do {
                    if(!buf.hasNext()) {
                        Collections.sort(list, new Comparator<String[]>() {
                            public int compare(String[] t, String[] t1) {
                                int c = t[0].compareTo(t1[0]);
                                return c == 0?t[1].compareTo(t1[1]):c;
                            }
                        });
                        StringBuilder buf1 = new StringBuilder();
                        Iterator i1 = list.iterator();

                        while(i1.hasNext()) {
                            String[] param2 = (String[])i1.next();
                            buf1.append(param2[0]).append("=").append(param2[1]);
                            if(i1.hasNext()) {
                                buf1.append('&');
                            }
                        }

                        return buf1.toString();
                    }

                    i = (String)buf.next();
                } while(i.startsWith("oauth_") && params.containsKey(i));

                param1 = request.getParameterValues(i);
            } while(param1 == null);

            Iterator i$ = param1.iterator();

            while(i$.hasNext()) {
                String value = (String)i$.next();
                addParam(i, value, list);
            }
        }
    }
    private static void addParam(String key, String value, List<String[]> list) {
        list.add(new String[]{UriComponent.encode(key, UriComponent.Type.UNRESERVED), value == null?"":UriComponent.encode(value, UriComponent.Type.UNRESERVED)});
    }

}
