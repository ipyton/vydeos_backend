package com.chen.blogbackend.util;


import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.resource.Emailv31;
import org.json.JSONArray;
import org.json.JSONObject;

public  class EmailSender {

    static String apiKey = System.getenv("MAILJET_API_KEY");
    static String apiSecret = System.getenv("MAILJET_API_SECRET");


    public static void send(String fromAddress,String toAddress, String code) throws Exception {
        // Get Mailjet API credentials from environment variables
        if (apiKey == null || apiSecret == null) {
            throw new RuntimeException("Missing Mailjet credentials in environment variables.");
        }
        AccountInfoValidator validator = new AccountInfoValidator();

        if( validator.validateUserEmail(fromAddress) && validator.validateUserEmail(toAddress) ) {
            throw new RuntimeException("Invalid email address.");
        }

        MailjetClient client = new MailjetClient("8ebdd494e7e8e821120834a1ee0ea11e", "1e8bd032b007f94345b157a05bd8877d");

        MailjetRequest request = new MailjetRequest(Emailv31.resource)
                .property(Emailv31.MESSAGES, new JSONArray()
                        .put(new JSONObject()
                                .put(Emailv31.Message.FROM, new JSONObject()
                                        .put("Email", fromAddress)
                                        .put("Name", "Noah"))
                                .put(Emailv31.Message.TO, new JSONArray()
                                        .put(new JSONObject()
                                                .put("Email", toAddress)
                                                .put("Name", "User")))
                                .put(Emailv31.Message.TEMPLATEID, 6948193) // <-- Replace with your actual template ID
                                .put(Emailv31.Message.TEMPLATELANGUAGE, true)
                                .put(Emailv31.Message.SUBJECT, "Verification code")
                                .put(Emailv31.Message.VARIABLES, new JSONObject()
                                        .put("code", code)
                        )
                ));

        MailjetResponse response = client.post(request);
        System.out.println(response.getStatus());
        System.out.println(response.getData());
    }

    public static void main(String[] args) throws Exception {
        System.out.println(apiKey);
        System.out.println(apiSecret);
        send("pingzi64@vydeo.xyz","czh1278341834@gmail.com","su" );
    }

}
