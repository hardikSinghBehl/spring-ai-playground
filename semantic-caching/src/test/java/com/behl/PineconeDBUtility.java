package com.behl;

import io.netty.util.internal.StringUtil;
import io.pinecone.clients.Pinecone;
import org.springframework.ai.vectorstore.pinecone.PineconeVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class PineconeDBUtility {

    @Autowired
    private PineconeVectorStore pineconeVectorStore;

    @Autowired
    private Environment environment;

    void clear() {
        Optional<Pinecone> nativeClient = pineconeVectorStore.getNativeClient();
        if (nativeClient.isPresent()) {
            var pinecone = nativeClient.get();
            var indexName = environment.getProperty("spring.ai.vectorstore.pinecone.index-name");
            var index = pinecone.getIndexConnection(indexName);
            index.deleteAll(StringUtil.EMPTY_STRING);
        } else {
            throw new IllegalStateException();
        }
    }

}