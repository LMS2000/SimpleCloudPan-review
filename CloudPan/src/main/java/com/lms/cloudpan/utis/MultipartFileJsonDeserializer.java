package com.lms.cloudpan.utis;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
//multipartfile的json序列化器
public class MultipartFileJsonDeserializer extends JsonDeserializer<MultipartFile> {
    @Override
    public MultipartFile deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        // 从JsonParser对象中读取文件内容等相关信息，并创建MultipartFile对象
        ObjectMapper mapper = (ObjectMapper) jsonParser.getCodec();
        JsonNode node = mapper.readTree(jsonParser);

        String fileName = node.get("name").asText();
        long fileSize = node.get("size").asLong();
        String contentType = node.get("type").asText();
        byte[] fileContent = node.get("content").binaryValue();

        return new MockMultipartFile(fileName, fileContent);
    }
}
