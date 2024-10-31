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
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

public class SMSGonderOA extends AbstractOutputAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(SMSGonderOA.class);

    private static final String logCevreSistem = "M";
    private static final String LOG_KIMLIK_NO = "Log Kimlik No";
    private static final String LOG_MUSTERI_NO = "Log Musteri No";
    private static final String GSM_NUMBER = "GSM Number";
    private static final String ONAY_KOD_ICERIK = "Onay Kod ICerik";
    private static final String JSON_TOKEN_NAME = "TKWEBT4Z";


    private static final String TK1 = "token1";
    private static final String TK2 = "token2";
    private static final String TK3 = "token3";
    private static final String TOKEN_URL = "token_url";
    private static final String SMS_URL = "sms_url";
    private static final String RESPONSE_CODE = "RESPONSE_CODE";
    private static final String RESPONSE_BODY = "RESPONSE_BODY";
    private static final String DOGRULAMA_KODU = "DOGRULAMA_KODU";

    private static String tk1;
    private static String tk2;
    private static String tk3;
    private static String token_url;
    private static String sms_url;
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
                LOGGER.warn("SMSGonderOA : TK1 property is not set in the file {}", configurationFileName);
            }

            tk2 = properties.getProperty(TK2);
            if (tk2 == null || tk2.isEmpty()) {
                LOGGER.warn("SMSGonderOA : TK2 property is not set in the file {}", configurationFileName);
            }
            tk3 = properties.getProperty(TK3);
            if (tk3 == null || tk3.isEmpty()) {
                LOGGER.warn("SMSGonderOA : TK3 property is not set in the file {}", configurationFileName);
            }
            token_url = properties.getProperty(TOKEN_URL);
            if (token_url == null || token_url.isEmpty()) {
                LOGGER.warn("SMSGonderOA : token_url property is not set in the file {}", configurationFileName);
            }
            sms_url = properties.getProperty(SMS_URL);
            if (token_url == null || token_url.isEmpty()) {
                LOGGER.warn("SMSGonderOA : sms_url property is not set in the file {}", configurationFileName);
            }
        } catch (Exception var16) {
            LOGGER.error("SMSGonderOA : ERROR {} ", var16.toString());
        }

    }

    @Override
    public int execute(OutputActionContext outputActionContext) throws Exception {
        String scenarioName = outputActionContext.getScenarioName();
        String kimlikNo = (String) outputActionContext.getParameter(LOG_KIMLIK_NO);
        String musteriNo = (String) outputActionContext.getParameter(LOG_MUSTERI_NO);
        String gsmNo = (String) outputActionContext.getParameter(GSM_NUMBER);
        String message = (String) outputActionContext.getParameter(ONAY_KOD_ICERIK);

        try {
            getWebTokenOA = new GetWebTokenOA();
            JSONObject jsonObject = getWebTokenOA.callTokenService(new TokenRequest(tk1, tk2, tk3, token_url, kimlikNo, musteriNo));
            if (jsonObject != null && jsonObject.getString(JSON_TOKEN_NAME) != null) {
                StringBuilder responseData = new StringBuilder();
                LOGGER.info("SMSGonderOA : SCENARIO {}, LOG_KIMLIK_NO {}, LOG_MUSTERI_NO {}, LOG_CEVRE_SISTEM {}, GSM_NUMBER {}, MESSSAGE {}",
                        scenarioName, kimlikNo, musteriNo, logCevreSistem,gsmNo,message);
                JSONObject smsRequest = new JSONObject()
                        .put("LogKimlikNo", kimlikNo)
                        .put("LogMusteriNo", musteriNo)
                        .put("LogCevreSistem", logCevreSistem)
                        .put("GSMNumber", gsmNo)
                        .put("OnayKodIcerik", message)
                        .put("WebToken", jsonObject.getString(JSON_TOKEN_NAME));

                StringEntity entity = new StringEntity(smsRequest.toString(), "UTF-8");

                HttpPost httpPost = new HttpPost(sms_url);

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
                LOGGER.info("SMSGonderOA : SCENARIO {}, RESPONSE_BODY {}, RESPONSE_CODE {}", scenarioName, response_body, response_code);
                JSONObject response = new JSONObject(response_body);

                outputActionContext.getReturnMap().put(RESPONSE_CODE, response_code);
                outputActionContext.getReturnMap().put(RESPONSE_BODY, response);
                outputActionContext.getReturnMap().put(DOGRULAMA_KODU, response.getString("DogrulamaKod"));
                return 0;

            } else {
                outputActionContext.getReturnMap().put(RESPONSE_BODY, jsonObject.toString());
                LOGGER.info("SMSGonderOA : Token Alma islemi basarısız {}", jsonObject.toString());
                return 0;

            }


        } catch (Exception e) {
            LOGGER.error("SMSGonderOA : ERROR {}", e.getMessage());
            return -1;
        }

    }

    @Override
    protected List<IOMParameter> getParameters() {
        List<IOMParameter> parameters = new ArrayList<>();
        parameters.add(new IOMParameter(LOG_KIMLIK_NO, "Kimlik Numarası"));
        parameters.add(new IOMParameter(LOG_MUSTERI_NO, "Musteri Numarası"));
        parameters.add(new IOMParameter(GSM_NUMBER, "Müşterinin GSM Numarası"));
        parameters.add(new IOMParameter(ONAY_KOD_ICERIK, "Onay Kod İçerik"));
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
        return "SMS Gonder Aksiyonu";
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
