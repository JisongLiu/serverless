package com.serverless.asgn1;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class AddressRequestHandler implements RequestHandler<AddressRequest, AddressResponse> {

    @Override
    public AddressResponse handleRequest(AddressRequest input, Context context) {
        context.getLogger().log("Input: " + input);

        // TODO: implement your handler
        return null;
    }

}
