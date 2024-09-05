# Using this sample

Requires 
- Azure CLI, 
- .NET 8, 
- JDK 17 or JDK 21
- Maven
- macOS/Linux/WSL2

See https://learn.microsoft.com/en-us/azure/storage/blobs/storage-encrypt-decrypt-blobs-key-vault?tabs=roles-azure-portal%2Cpackages-dotnetcli#assign-a-role-to-your-microsoft-entra-user to prepare the required Azure resources
and grant your user access to Key Vault and Blob Storage.

```bash
export KEY_VAULT_NAME="mykeyvault"
export STORAGE_ACCOUNT_NAME="mystorageaccount"

az login

git clone https://github.com/joergjo/azure-blobencryption-samples.git
cd ./azure-blobencryption-samples/dotnet/BlobEncryptionKeyVault/
# use any multiple of 4, but 4 MB suffice
dd if=/dev/urandom of=random_file.bin bs=1M count=4

dotnet run
diff random_file.bin random_file_copy.bin 

cd ../../java/blobencrypt/
mvn exec:java -Dexec.mainClass="com.example.App"
```
