# chatgpt-ui api server

> 환경설정파일 위치 : /src/main/resources/properties/chatgpt.properties 


### 1. jar 실행
```shell
java -jar ./target/chatgptAPI-0.0.1-SNAPSHOT.jar --server.port=18080
```

### 2. docker로 실행할 때
```shell
docker build -t chatgpt-api:0.1 .
docker run --name chatgpt-api -d -p 18080:8080 chatgpt-api:0.1
```

### 3. 요청 샘플
```shell
curl -X POST -H "Content-Type: application/json" -d '{"message":"주민등록번호 입력창"}' http://localhost:18080/chatgpt-ui/v1/send
```

### 4. 응답 샘플
```json
{
  "input_message": "주민등록번호 입력창",
  "output_message": {
    "created": 1687765860,
    "usage": {
      "completion_tokens": 237,
      "prompt_tokens": 62,
      "total_tokens": 299
    },
    "model": "gpt-3.5-turbo-0301",
    "id": "chatcmpl-7VbHwVemuuA1ifZXcIK3oKaHUYmDd",
    "choices": [
      {
        "finish_reason": "stop",
        "index": 0,
        "message": {
          "role": "assistant",
          "content": "아래 코드는 Vue 3의 템플릿 문법을 사용하여 주민등록번호 입력창을 생성하는 예시입니다.\n\n```html\n<template>\n  <div>\n    <label for=\"resident-id\">주민등록번호:</label>\n    <input type=\"text\" id=\"resident-id\" v-model=\"residentId\" maxlength=\"13\">\n  </div>\n</template>\n\n<script>\n  export default {\n    data() {\n      return {\n        residentId: ''\n      }\n    }\n  }\n</script>\n```\n\n위 코드에서는 `label` 태그를 사용하여 입력창의 라벨을 표시하고, `input` 태그를 사용하여 실제 입력창을 생성합니다. `v-model` 디렉티브를 사용하여 입력값과 `residentId` 데이터를 양방향 바인딩하고, `maxlength` 속성을 사용하여 입력 가능한 최대 길이를 13자리로 설정합니다."
        }
      }
    ],
    "object": "chat.completion"
  },
  "content": {
    "code_block": "<template>  <div>    <label for=\"resident-id\">주민등록번호:</label>    <input type=\"text\" id=\"resident-id\" v-model=\"residentId\" maxlength=\"13\">  </div></template><script>  export default {    data() {      return {        residentId: ''      }    }  }</script>",
    "code_type": "html"
  }
}
```



### 참고자료
* [openai api 공식 문서](https://platform.openai.com/docs/api-reference)

