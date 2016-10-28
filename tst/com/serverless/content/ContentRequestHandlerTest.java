package com.serverless.content;

import java.io.IOException;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.amazonaws.services.lambda.runtime.Context;

import com.serverless.content.ContentRequest;
import com.serverless.content.ContentRequestHandler;
import com.serverless.content.ContentResponse;

/**
 * A simple test harness for locally invoking your Lambda function handler.
 */
public class ContentRequestHandlerTest {

    private static ContentRequest input;

    @BeforeClass
    public static void createInput() throws IOException {
        input = TestUtils.parse("dummy.json", ContentRequest.class);
    }

    private Context createContext() {
        TestContext ctx = new TestContext();

        // TODO: customize your context here if needed.
        ctx.setFunctionName("Your Function Name");

        return ctx;
    }

    @Test
    public void testGetContent() {
        ContentRequestHandler handler = new ContentRequestHandler();
        Context ctx = createContext();

        ContentResponse output = handler.handleRequest(input, ctx);
        List<Content> items = output.getItems();
        for (Content item : items) {
        	System.out.println(item.getId());
        	System.out.println(item.getName());
        	System.out.println(item.getType());
        }

        // TODO: validate output here if needed.
        if (output != null) {
            System.out.println(output.toString());
        }
    }
}
