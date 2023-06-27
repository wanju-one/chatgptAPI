package com.initech.chatgptapi.service;

import com.initech.chatgptapi.common.CodeType;
import com.initech.chatgptapi.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ChatgptapiServiceV1 implements ChatgptapiService{

    private static JSONParser JsonParser = new JSONParser();
    private final String CODE_DELIMITER = "```";

    @Value("#{chatgpt['openai.apiKey']}")
    private String OPENAI_KEY;

    @Value("#{chatgpt['openai.apiUrl']}")
    private String OPENAI_URL;

    @Value("#{chatgpt['openai.model']}")
    private String OPENAI_MODEL;

    @Override
    public Map sendMessage(Map param) throws Exception{
        System.out.println(OPENAI_KEY);
        System.out.println(OPENAI_URL);
        System.out.println(OPENAI_MODEL);


        String message = String.valueOf(param.get("message"));
        Map<String, Object> resultMap = new HashMap<>();

        // 입력값 검증, JSON데이터 생성
        JSONObject sendData = createJsonObject(message);
        resultMap.put("input_message", message);
        log.info("input_message="+message.toString());
        if(sendData == null) return null;

        // HTTP 송수신
        JSONObject response = callOpenai(sendData);
        resultMap.put("output_message", response);
        log.info("output_message="+response.toJSONString());
        if(response == null) return null;

        // 응답에서 코드 파싱
        Map<String, Object> content = getContents(response);
        resultMap.put("content", content);
        log.info("content="+content.toString());
        if(content == null) return null;

        return resultMap;
    }

    private JSONObject createJsonObject(String message) throws Exception{
        if("".equals(message) || message == null) return null;

        JSONObject data = new JSONObject();
        /**
         * Model별 API 규격이 다르기 때문에 쉽게 변경하긴 어려워보인다.
         */
        data.put("model", OPENAI_MODEL);
        /**
         * temperature값은 0~1 무한대이며
         * 1에 가까울수록 창의적이고 다양한 대답을 준다.
         * 0에 가까울수록 정적인 응답을 한다. 이 값을 계속 바꿔가며 테스트를 해봐야할듯
         */
        data.put("temperature", 0.7);
        /**
         * max_tokens 이 300이면
         * chatgpt 응답 메시지가 300단어로 구성된다.
         * 토큰당 요금이 청구되지만 제한을 둘 수 없어 무한대로 설정하였다.
         * ex) 안녕하세요 제 이름은 이완주 입니다.  (5토큰 사용)
         */
//        data.put("max_tokens", 300);


        /**
         * FIXME 나중에 화면에서 Composition Mode에 따라 content 수정필요
         */
        JSONArray messages = new JSONArray();
        JSONObject message1 = new JSONObject();
        JSONObject message2 = new JSONObject();
        JSONObject message3 = new JSONObject();
        message1.put("role", "system");
        message1.put("content", "모든 답변은 vue3 html 코드로 생성해줘");
        message2.put("role", "assistant");
        message2.put("content", "모든 답변은 vue3 html 코드로 생성해줘");
        message3.put("role", "user");
        message3.put("content", message); // 화면에서 입력받은 메세지
        messages.add(message1);
        messages.add(message2);
        messages.add(message3);
        data.put("messages", messages);

//        log.info("sendMessage="+data.toJSONString());
        return data;
    }

    private JSONObject callOpenai(JSONObject sendData) throws Exception{
        if(sendData == null) return null;

        JSONObject response = new JSONObject();

        URL url = new URL(OPENAI_URL+"/v1/chat/completions");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try(AutoCloseable a = () -> conn.disconnect()){
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", OPENAI_KEY);
            conn.setDoOutput(true);

            try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), "utf-8"))){
                bw.write(sendData.toJSONString());
                bw.flush();
            }// end-writer

            if(200 != conn.getResponseCode()) {
                log.error("응답이 200이 아님["+conn.getResponseCode()+"]");
                return null;
            }

            try(BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))){
                StringBuilder sb = new StringBuilder();
                String readLine = null;
                while ((readLine = br.readLine()) != null) {
                    sb.append(readLine.trim());
                }
                Object obj = JsonParser.parse(sb.toString());
                if(obj instanceof JSONObject){
                    response = (JSONObject) obj;
                }else{
                    log.error("일단 JSONObject만 수신할것");
                    return null;
                }
            } // end-reader
        } // end-conn
//        log.info("response="+response.toString());
        return response;
    }

    private Map<String, Object> getContents(JSONObject response) throws Exception {
        if(response == null) return null;
        // json to map
        HashMap<String, Object> responseMap = (HashMap<String, Object>) JsonUtil.jsonToMap(response.toJSONString());

        Object obj = responseMap.get("choices");
        HashMap<String, Object> choices =  (HashMap<String, Object>)((List<?>) obj).get(0);
        HashMap<String, Object> message = (HashMap<String, Object>) choices.get("message");
        String content = (String) message.get("content");

        HashMap<String, Object> codeBlock = getCodeBlock(content);
        return codeBlock;
    }

    private HashMap<String, Object> getCodeBlock(String content) {
        HashMap<String, Object> resultMap = new HashMap<String, Object>();

        int start_point = 0;
        for(CodeType codeType : CodeType.values()){
            start_point = content.indexOf(CODE_DELIMITER+codeType);
            if(start_point > -1){
                resultMap.put("code_type", codeType);
                start_point+=codeType.toString().length();
                start_point+=CODE_DELIMITER.length();
                break;
            }
        }
        if(start_point == -1){
            start_point = content.indexOf(CODE_DELIMITER);
            resultMap.put("code_type", null);
            start_point+=CODE_DELIMITER.length();
        }

        int end_point = content.lastIndexOf(CODE_DELIMITER);
        // 코드블럭 끝까지 수신하지 못하면 종료
        if(end_point == -1) return null;

        String codeBlock = content.substring(start_point, end_point);

        // 전문에 코드 블록이 2개 이상일 경우 종료
        if(codeBlock.indexOf(CODE_DELIMITER)>-1) return null;

        codeBlock = codeBlock.replace("\n","");
        resultMap.put("code_block", codeBlock);
        return resultMap;
    }

}
