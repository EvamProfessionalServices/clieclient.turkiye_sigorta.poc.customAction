package org.action;

import com.evam.sdk.outputaction.AbstractOutputAction;
import com.evam.sdk.outputaction.IOMParameter;
import com.evam.sdk.outputaction.OutputActionContext;
import com.evam.sdk.outputaction.model.DesignerMetaParameters;
import com.evam.sdk.outputaction.model.ReturnParameter;
import com.evam.sdk.outputaction.model.ReturnType;
import com.evam.utils.util.property.FileDefinitions;
import org.action.model.TokenRequest;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

public class EMAILGonderOA extends AbstractOutputAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(EMAILGonderOA.class);

    private static final String logCevreSistem = "M";
    private static final String LOG_KIMLIK_NO = "Log Kimlik No";
    private static final String LOG_MUSTERI_NO = "Log Musteri No";
    private static final String EMAIL_TO = "To";
    private static final String SUBJECT = "Subject";
    private static final String BODY = "Body";
    private static final String JSON_TOKEN_NAME = "TKWEBT4Z";


    private static final String TK1 = "token1";
    private static final String TK2 = "token2";
    private static final String TK3 = "token3";
    private static final String TOKEN_URL = "token_url";
    private static final String EMAIL_URL = "email_url";
    private static final String RESPONSE_CODE = "RESPONSE_CODE";
    private static final String RESPONSE_BODY = "RESPONSE_BODY";
    private static final String DOGRULAMA_KODU = "DOGRULAMA_KODU";

    private static String tk1;
    private static String tk2;
    private static String tk3;
    private static String token_url;
    private static String email_url;
    private GetWebTokenOA getWebTokenOA;


    @Override
    public synchronized void init() {
        Properties properties = new Properties();
        String configurationFileName = FileDefinitions.CONF_FOLDER + "IntegrationConfig.properties";
        try {
            FileInputStream fileInputStream = new FileInputStream(configurationFileName);
            properties.load(fileInputStream);
            tk1 = properties.getProperty(TK1);
            if (tk1 == null || tk1.isEmpty()) {
                LOGGER.warn("EMAILGonderOA : TK1 property is not set in the file {}", configurationFileName);
            }

            tk2 = properties.getProperty(TK2);
            if (tk2 == null || tk2.isEmpty()) {
                LOGGER.warn("EMAILGonderOA : TK2 property is not set in the file {}", configurationFileName);
            }
            tk3 = properties.getProperty(TK3);
            if (tk3 == null || tk3.isEmpty()) {
                LOGGER.warn("EMAILGonderOA : TK3 property is not set in the file {}", configurationFileName);
            }
            token_url = properties.getProperty(TOKEN_URL);
            if (token_url == null || token_url.isEmpty()) {
                LOGGER.warn("EMAILGonderOA : token_url property is not set in the file {}", configurationFileName);
            }
            email_url = properties.getProperty(EMAIL_URL);
            if (token_url == null || token_url.isEmpty()) {
                LOGGER.warn("EMAILGonderOA : sms_url property is not set in the file {}", configurationFileName);
            }
        } catch (Exception var16) {
            LOGGER.error("EMAILGonderOA : ERROR {} ", var16.toString());
        }

    }

    @Override
    public int execute(OutputActionContext outputActionContext) throws Exception {
        String scenarioName = outputActionContext.getScenarioName();
        String kimlikNo = (String) outputActionContext.getParameter(LOG_KIMLIK_NO);
        String musteriNo = (String) outputActionContext.getParameter(LOG_MUSTERI_NO);
        String email_to = (String) outputActionContext.getParameter(EMAIL_TO);
        String subject = (String) outputActionContext.getParameter(SUBJECT);
        String body = (String) outputActionContext.getParameter(BODY);

        try {
            getWebTokenOA = new GetWebTokenOA();
            JSONObject jsonObject = getWebTokenOA.callTokenService(new TokenRequest(tk1, tk2, tk3, token_url, kimlikNo, musteriNo));
            if (jsonObject != null && jsonObject.getString(JSON_TOKEN_NAME) != null) {
                StringBuilder responseData = new StringBuilder();
                LOGGER.debug("EMAILGonderOA : SCENARIO {}, LOG_KIMLIK_NO {}, LOG_MUSTERI_NO {}, LOG_CEVRE_SISTEM {}, EMAIL_TO {}, SUBJECT {}, BODY{}",
                        scenarioName, kimlikNo, musteriNo, logCevreSistem,email_to,subject,body);
                JSONArray emailToArray = new JSONArray().put(email_to);
                JSONObject smsRequest = new JSONObject()
                        .put("LogKimlikNo", kimlikNo)
                        .put("LogMusteriNo", musteriNo)
                        .put("LogCevreSistem", logCevreSistem)
                        .put("To", emailToArray)
                        .put("Cc", new JSONArray())
                        .put("Bcc", new JSONArray())
                        .put("Attachments", new JSONArray())
                        .put("isHtml",false)
                        .put("SendDate", Instant.now().toString())
                        .put("Subject", subject)
                        .put("Body", body)
                        .put("WebToken", jsonObject.getString(JSON_TOKEN_NAME));

                StringEntity entity = new StringEntity(smsRequest.toString(), "UTF-8");

                HttpPost httpPost = new HttpPost(email_url);

                httpPost.setHeader("Content-Type", "application/json");

                httpPost.setEntity(entity);
                CloseableHttpClient httpclient = HttpClients.createDefault();

                CloseableHttpResponse httpResponse = httpclient.execute(httpPost);

                Scanner scanner = new Scanner(httpResponse.getEntity().getContent());
                while (scanner.hasNext()) {
                    responseData.append(scanner.nextLine().trim());
                }
                scanner.close();

                String response_code = String.valueOf(httpResponse.getStatusLine().getStatusCode());
                String response_body = responseData.toString();
                LOGGER.info("EMAILGonderOA : SCENARIO {}, RESPONSE_BODY {}, RESPONSE_CODE {}", scenarioName, response_body, response_code);
                JSONObject response = new JSONObject(response_body);

                outputActionContext.getReturnMap().put(RESPONSE_CODE, response_code);
                outputActionContext.getReturnMap().put(RESPONSE_BODY, response);
                return 0;

            } else {
                outputActionContext.getReturnMap().put(RESPONSE_BODY, jsonObject.toString());
                LOGGER.info("EMAILGonderOA : Token Alma islemi basarısız {}", jsonObject.toString());
                return 0;

            }


        } catch (Exception e) {
            LOGGER.error("EMAILGonderOA : ERROR {}", e.getMessage());
            return -1;
        }

    }

    @Override
    protected List<IOMParameter> getParameters() {
        List<IOMParameter> parameters = new ArrayList<>();
        parameters.add(new IOMParameter(LOG_KIMLIK_NO, "Kimlik Numarası"));
        parameters.add(new IOMParameter(LOG_MUSTERI_NO, "Musteri Numarası"));
        parameters.add(new IOMParameter(EMAIL_TO, "Email Adresi"));
        parameters.add(new IOMParameter(SUBJECT, "Email Konusu"));
        parameters.add(new IOMParameter(BODY, "Email Metni"));
        return parameters;
    }

    @Override
    public boolean actionInputStringShouldBeEvaluated() {
        return false;
    }

    @Override
    public String getVersion() {
        return "v.1.0";
    }

    @Override
    public boolean isReturnable() {
        return true;
    }

    @Override
    public String getDescription() {
        return "Email Gonder Aksiyonu";
    }

    @Override
    public ReturnParameter[] getRetParams(DesignerMetaParameters designerMetaParameters) {
        return new ReturnParameter[]{
                new ReturnParameter(RESPONSE_CODE, ReturnType.String),
                new ReturnParameter(RESPONSE_BODY, ReturnType.String),
                new ReturnParameter(DOGRULAMA_KODU, ReturnType.String)
        };
    }
}
