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

    public static void send(String emailAddress, String code) throws Exception {
        // Get Mailjet API credentials from environment variables
        if (apiKey == null || apiSecret == null) {
            throw new RuntimeException("Missing Mailjet credentials in environment variables.");
        }

        MailjetClient client = new MailjetClient(apiKey, apiSecret);

        MailjetRequest request = new MailjetRequest(Emailv31.resource)
                .property(Emailv31.MESSAGES, new JSONArray()
                        .put(new JSONObject()
                                .put(Emailv31.Message.FROM, new JSONObject()
                                        .put("Email", "czh1278341834@gmail.com")
                                        .put("Name", "Noah"))
                                .put(Emailv31.Message.TO, new JSONArray()
                                        .put(new JSONObject()
                                                .put("Email", emailAddress)
                                                .put("Name", "User")))
                                .put(Emailv31.Message.SUBJECT, "Hello from Mailjet")
                                .put(Emailv31.Message.TEXTPART, "This is a test email sent via Mailjet and Java.")
                                .put(Emailv31.Message.HTMLPART, "<h3>Hello from Mailjet</h3><p>This is a test email.</p>")
                        )
                );

        MailjetResponse response = client.post(request);
        System.out.println(response.getStatus());
        System.out.println(response.getData());
    }

}
