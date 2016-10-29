package com.serverless.comment;

import java.io.IOException;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.amazonaws.services.lambda.runtime.Context;

import com.serverless.comment.CommentRequest;
import com.serverless.comment.CommentRequestHandler;
import com.serverless.comment.CommentResponse;

/**
 * A simple test harness for locally invoking your Lambda function handler.
 */
public class CommentRequestHandlerTest {

    private static CommentRequest input;

    @BeforeClass
    public static void createInput() throws IOException {
        input = TestUtils.parse("dummy.json", CommentRequest.class);
    }

    private Context createContext() {
        TestContext ctx = new TestContext();

        // TODO: customize your context here if needed.
        ctx.setFunctionName("Your Function Name");

        return ctx;
    }

    @Test
    public void testGetContent() {
        CommentRequestHandler handler = new CommentRequestHandler();
        Context ctx = createContext();

        CommentResponse output = handler.handleRequest(input, ctx);
        List<Comment> items = output.getItems();
        for (Comment item : items) {
        	System.out.println(item.getId());
        	System.out.println(item.getUser());
        	System.out.println(item.getContent());
        	System.out.println(item.getComment());
        }

        // TODO: validate output here if needed.
        if (output != null) {
            System.out.println(output.toString());
        }
    }
}
