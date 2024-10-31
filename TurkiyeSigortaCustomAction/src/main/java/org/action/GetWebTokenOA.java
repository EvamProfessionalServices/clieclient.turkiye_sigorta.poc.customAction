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
import java.util.Scanner;

public class GetWebTokenOA {
    private static final Logger LOGGER = LoggerFactory.getLogger(GetWebTokenOA.class);
    private static final String logCevreSistem = "M";

    public JSONObject callTokenService(TokenRequest tokenRequest)  {

        try {
            StringBuilder responseData = new StringBuilder();
            LOGGER.info("GetWebTokenOA :  LOG_KIMLIK_NO {}, LOG_MUSTERI_NO {}, LOG_CEVRE_SISTEM {}",
                    tokenRequest.getLog_kimlik_no(),tokenRequest.getLog_musteri_no(),logCevreSistem);
            JSONObject jsonObject = new JSONObject()
                    .put("TK1",tokenRequest.getTk1())
                    .put("TK2",tokenRequest.getTk2())
                    .put("TK3",tokenRequest.getTk3())
                    .put("LogKimlikNo",tokenRequest.getLog_kimlik_no())
                    .put("LogMusteriNo",tokenRequest.getLog_musteri_no())
                    .put("LogCevreSistem",logCevreSistem);

            StringEntity entity = new StringEntity(jsonObject.toString(), "UTF-8");

            HttpPost httpPost = new HttpPost(tokenRequest.getApiUrl());

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
            LOGGER.info("GetWebTokenOA :  RESPONSE_BODY {}, RESPONSE_CODE {}", response_body,response_code);
            return new JSONObject(response_body);

        }
        catch (Exception e){
            LOGGER.error("GetWebTokenOA : ERROR {}",e.getMessage());
            return null;
        }

    }


}
