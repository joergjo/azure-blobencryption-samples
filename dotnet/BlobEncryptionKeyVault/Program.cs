using Azure.Identity;
using Azure.Security.KeyVault.Keys;
using Azure.Security.KeyVault.Keys.Cryptography;
using Azure.Storage;
using Azure.Storage.Blobs;
using Azure.Storage.Blobs.Specialized;

const string original = "random_file.bin";
const string copy = "random_file_copy.bin";
const string keyName = "testRSAKey";
const string containerName = "test-container";
const string blobName = "testBlob";

var keyVaultName = Environment.GetEnvironmentVariable("KEY_VAULT_NAME");
var accountName = Environment.GetEnvironmentVariable("STORAGE_ACCOUNT_NAME");
var keyVaultUri = $"https://{keyVaultName}.vault.azure.net";

var tokenCredential = new DefaultAzureCredential();
var keyClient = new KeyClient(new Uri(keyVaultUri), tokenCredential);
var key = await keyClient.CreateKeyAsync(keyName, KeyType.Rsa);
var cryptoClient = keyClient.GetCryptographyClient(key.Value.Name, key.Value.Properties.Version);
var keyResolver = new KeyResolver(tokenCredential);
var encryptionOptions = 
    new ClientSideEncryptionOptions (ClientSideEncryptionVersion.V2_0)
    {
        KeyEncryptionKey = cryptoClient,
        KeyResolver = keyResolver,
        KeyWrapAlgorithm = "RSA-OAEP"
    };

var blobUri = new Uri($"https://{accountName}.blob.core.windows.net");
var options = new SpecializedBlobClientOptions { ClientSideEncryption = encryptionOptions };
var blob = new BlobServiceClient(blobUri, tokenCredential, options)
    .GetBlobContainerClient(containerName)
    .GetBlobClient(blobName);

var input = new FileInfo(original);
using var blobContent = input.OpenRead();
await blob.UploadAsync(blobContent, overwrite: true);
Console.WriteLine("Uploaded {0} bytes to blob storage.", input.Length);

await blob.DownloadToAsync(copy);
var output = new FileInfo(copy);
Console.WriteLine("Downloaded {0} bytes from blob storage.", output.Length);
