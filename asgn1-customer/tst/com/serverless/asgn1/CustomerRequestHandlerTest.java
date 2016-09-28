package com.serverless.asgn1;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.amazonaws.services.lambda.runtime.Context;

import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.serverless.asgn1.CustomerRequestHandler;

/**
 * A simple test harness for locally invoking your Lambda function handler.
 */
public class CustomerRequestHandlerTest {

    private static CustomerRequest input;

    @BeforeClass
    public static void createInput() throws IOException {
        input = TestUtils.parse("dummy.json", CustomerRequest.class);
    }

    private Context createContext() {
        TestContext ctx = new TestContext();

        // TODO: customize your context here if needed.
        ctx.setFunctionName("Your Function Name");

        return ctx;
    }

    @Test
    public void testGetCustomer() {
        CustomerRequestHandler handler = new CustomerRequestHandler();
        Context ctx = createContext();

        CustomerResponse output = handler.handleRequest(input, ctx);

        // TODO: validate output here if needed.
        if (output != null) {
            System.out.println(output.toString());
        }
    }
}
