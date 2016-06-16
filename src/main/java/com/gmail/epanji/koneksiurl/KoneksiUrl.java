package com.gmail.epanji.koneksiurl;

import android.content.ContentValues;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unused")
public class KoneksiUrl {

    private static final String BOUNDARY = "*****";
    private static final String EOL = "\r\n";
    private static final String SIGN = "--";
    private static final String CHARSET = "charset=UTF-8";

    private static final String KEY_CONNECTION = "Connection";
    private static final String VAL_CONNECTION = "Keep-Alive";

    private static final String KEY_CACHE = "Cache-Control";
    private static final String VAL_CACHE = "no-cache";

    private static final String KEY_CONTENT = "Content-Type";
    private static final String VAL_CONTENT = "multipart/form-data;boundary=" + BOUNDARY;

    private static final String KEY_ENCTYPE = "enctype";
    private static final String VAL_ENCTYPE = "multipart/form-data";

    private static final String FIELD_TEXT_OPEN = "Content-Disposition:form-data;name=\"";
    private static final String FIELD_TEXT_CLOSE = "\"" + EOL;

    private static final String VALUE_TEXT_OPEN = "Content-Type:text/plain;" + CHARSET + EOL + EOL;
    private static final String VALUE_TEXT_CLOSE = EOL;

    private static final String FIELD_FILE_OPEN = "Content-Disposition:form-data;name=\"";
    private static final String FIELD_FILE_MID = "\";filename=\"";
    private static final String FIELD_FILE_CLOSE = "\"" + EOL + EOL;

    private String mUrl;
    private ContentValues mParams;
    private HashMap<String, File> mFiles;

    private String execute() {
        String response = "";
        HttpURLConnection huc = null;
        if (mUrl != null) {
            try {
                URL url = new URL(mUrl);
                huc = (HttpURLConnection) url.openConnection();
                if (mParams != null || mFiles != null) {
                    huc.setDoOutput(true);
                    huc.setChunkedStreamingMode(0);

                    String params = this.formatPostParams(mParams);
                    if (mFiles != null) {
                        huc = this.setMultipartHead(huc);
                    }
                    OutputStream os = new BufferedOutputStream(huc.getOutputStream());
                    writeStream(os, params);
                }
                int responseCode = huc.getResponseCode();
                switch (responseCode) {
                    case 200:
                    case 201:
                        InputStream is = new BufferedInputStream(huc.getInputStream());
                        response = readStream(is);
                        break;

                    default:
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(huc != null) {
                    huc.disconnect();
                }
            }
        }
        return response;
    }

    @SuppressWarnings("unused")
    public String execute(String url) {
        this.mUrl = url;
        return this.execute();
    }

    @SuppressWarnings("unused")
    public String execute(String url, ContentValues params) {
        this.mUrl = url;
        this.mParams = params;
        return this.execute();
    }

    @SuppressWarnings("unused")
    public String execute(String url, ContentValues params, HashMap<String, File> files) {
        this.mUrl = url;
        this.mParams = params;
        this.mFiles = files;
        return this.execute();
    }

    private String formatPostParams(ContentValues cv) {
        StringBuilder sb = new StringBuilder();
        Set<Map.Entry<String, Object>> set = cv.valueSet();
        if (cv.size() > 0) {
            for (Map.Entry entry : set) {
                sb.insert(0, String.valueOf(entry.getValue()));
                sb.insert(0, '=');
                sb.insert(0, String.valueOf(entry.getKey()));
                sb.insert(0, '&');
            }
            sb.deleteCharAt(0);
        }
        return sb.toString();
    }

    private String readStream(InputStream is){
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        try {
            br = new BufferedReader(new InputStreamReader(is, "utf-8"));
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    private void writeStream(OutputStream os, String params) {
        DataOutputStream dos = null;
        try {
            dos = new DataOutputStream(os);
            if (mFiles == null) {
                dos.writeBytes(params);
            } else {
                dos = multipartParams(dos, params);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (dos != null) {
                    dos.close();
                }
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private DataOutputStream multipartParams(DataOutputStream dos, String params)
            throws IOException {
        if (! params.equalsIgnoreCase("")) {
            String[] kvs = params.split("&");
            for (String kv : kvs) {
                String[] s = kv.split("=");
                dos.writeBytes(SIGN + BOUNDARY + EOL);
                dos.writeBytes(FIELD_TEXT_OPEN);
                dos.writeBytes(s[0]);
                dos.writeBytes(FIELD_TEXT_CLOSE);
                dos.writeBytes(VALUE_TEXT_OPEN);
                dos.writeBytes(s[1]);
                dos.writeBytes(VALUE_TEXT_CLOSE);
                dos.flush();
            }
        }
        if (! mFiles.isEmpty()) {
            Set<Map.Entry<String, File>> set = mFiles.entrySet();
            for (Map.Entry<String, File> o : set) {
                dos.writeBytes(SIGN + BOUNDARY + EOL);
                dos.writeBytes(FIELD_FILE_OPEN);
                dos.writeBytes(o.getKey());
                dos.writeBytes(FIELD_FILE_MID);
                dos.writeBytes(o.getValue().getName());
                dos.writeBytes(FIELD_FILE_CLOSE);
                dos.write(byteArray(o.getValue()));
                dos.writeBytes(EOL);
                dos.flush();
            }
        }
        dos.writeBytes(EOL);
        dos.writeBytes(SIGN + BOUNDARY + SIGN + EOL);
        return dos;
    }

    private HttpURLConnection setMultipartHead(HttpURLConnection huc) {
        huc.setUseCaches(false);
        huc.setDoOutput(true);
        huc.setDoInput(true);
        huc.setRequestProperty(KEY_CONNECTION, VAL_CONNECTION);
        huc.setRequestProperty(KEY_CACHE, VAL_CACHE);
        huc.setRequestProperty(KEY_ENCTYPE, VAL_ENCTYPE);
        huc.setRequestProperty(KEY_CONTENT, VAL_CONTENT);
        return huc;
    }

    private byte[] byteArray(File f) throws IOException {
        ByteArrayOutputStream baos = null;
        InputStream fis = null;
        try {
            byte[] buffer = new byte[4096];
            baos = new ByteArrayOutputStream();
            fis = new FileInputStream(f);
            int read;
            while ((read = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, read);
            }
        } finally {
            if (baos != null) baos.close();
            if (fis != null) fis.close();
        }
        return baos.toByteArray();
    }

}
