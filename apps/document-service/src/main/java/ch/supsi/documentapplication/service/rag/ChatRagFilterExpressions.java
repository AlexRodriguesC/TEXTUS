package ch.supsi.documentapplication.service.rag;

import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;

public final class ChatRagFilterExpressions {

    private ChatRagFilterExpressions() {}

    public static Filter.Expression spaceIdEquals(long spaceId) {
        return new FilterExpressionBuilder().eq("spaceId", spaceId).build();
    }
}
