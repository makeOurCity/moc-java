package city.makeour.moc.examples;

import java.util.UUID;

import org.springframework.web.client.RestClientResponseException;

import city.makeour.moc.MocClient;
import city.makeour.ngsi.v2.model.CreateEntityRequest;

public class Auth {

    public static void main(String[] args) {
        // Create a new MocClient instance
        MocClient mocClient = new MocClient("https://orion.sandbox.makeour.city");

        // Set the Cognito auth info
        mocClient.setMocAuthInfo("your_cognito_user_pool_id", "your_cognito_client_id");

        // Authenticate with username and password
        try {
            mocClient.auth("your_username", "your_password");
        } catch (Exception e) {
            e.printStackTrace();
        }

        String uuid = UUID.randomUUID().toString();

        CreateEntityRequest entity = new CreateEntityRequest();
        entity.setType("TestEntity");
        entity.setId("urn:ngsi-ld:TestEntity:" + uuid);
        try {
            mocClient.createEntity("application/json", entity);
        } catch (RestClientResponseException e) {
            System.err.println("Error creating entity: " + e.getMessage());
        }

    }
}
