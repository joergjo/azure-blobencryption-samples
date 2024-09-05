package example.com;

import com.azure.core.cryptography.AsyncKeyEncryptionKey;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.keys.KeyClient;
import com.azure.security.keyvault.keys.KeyClientBuilder;
import com.azure.security.keyvault.keys.cryptography.KeyEncryptionKeyClientBuilder;
import com.azure.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
import com.azure.storage.blob.specialized.cryptography.EncryptedBlobClient;
import com.azure.storage.blob.specialized.cryptography.EncryptedBlobClientBuilder;
import com.azure.storage.blob.specialized.cryptography.EncryptionVersion;

/**
 * A sample Azure application.
 */
public class App {
    private static final String keyName = "testRSAKey";
    private static final String blobName = "testBlob";
    private static final String containerName = "test-container";

    public static void main(String[] args) {
        String keyVaultUrl = String.format("https://%s.vault.azure.net/", System.getenv("KEY_VAULT_NAME"));
        String endpoint = String.format("https://%s.blob.core.windows.net", System.getenv("STORAGE_ACCOUNT_NAME"));

        DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();
        KeyClient keyClient = new KeyClientBuilder()
            .vaultUrl(keyVaultUrl)
            .credential(tokenCredential)
            .buildClient();
        KeyVaultKey rsaKey = keyClient.getKey(keyName);
        AsyncKeyEncryptionKey akek = new KeyEncryptionKeyClientBuilder()
            .credential(tokenCredential)
            .buildAsyncKeyEncryptionKey(rsaKey.getId())
            .block();

        String fileName = String.format("%s.bin", blobName);    
        System.out.println(String.format("Downloading blob to %s", fileName));
        EncryptedBlobClient client = new EncryptedBlobClientBuilder(EncryptionVersion.V2)
            .key(akek, KeyWrapAlgorithm.RSA_OAEP.toString())
            .credential(tokenCredential)
            .endpoint(endpoint)
            .containerName(containerName)
            .blobName(blobName)
            .buildEncryptedBlobClient();
        client.downloadToFile(fileName, true);
        System.out.println(String.format("Downloaded blob to %s", fileName));
    }

}
