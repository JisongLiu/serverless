package com.serverless.address;
import com.smartystreets.api.exceptions.SmartyException;
import com.smartystreets.api.us_street.*;

import java.io.IOException;
import java.util.ArrayList;

public class AddressValidator {
	public String[] validateBySmartyStreet(String street, String city, String state) {
        Client client = new ClientBuilder("8453665d-bdee-57a1-66b1-d954bd22062c", "MGvhcihypS3j1C4hLIx1").build();

        Lookup lookup = new Lookup();
        lookup.setStreet(street);
        lookup.setCity(city);
        lookup.setState(state);
        
        try {
            client.send(lookup);
        }
        catch (SmartyException ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }

        ArrayList<Candidate> results = lookup.getResult();

        if (results.isEmpty()) {
            System.out.println("No candidates. This means the address is not valid.");
            return null;
        }

        Candidate firstCandidate = results.get(0);
        
        String[] result = new String[5];
        
        String plus4Code = firstCandidate.getComponents().getPlus4Code();
        String zipCode = firstCandidate.getComponents().getZipCode();
        result[0] = firstCandidate.getDeliveryPointBarcode();
        result[1] = firstCandidate.getComponents().getCityName();
        result[2] = firstCandidate.getDeliveryLine1();
        result[3] = firstCandidate.getComponents().getState();
        result[4] = zipCode+"-"+plus4Code;
        
        return result;
        
    }
}


