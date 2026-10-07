import base64
import json
import subprocess
import tempfile
import os

PROJECT = "prj-uc002-bigdata-dev-f2e2"
INSTANCE = "keymaker-spanner"
DATABASE = "keymaker-db"

SAFE_NAME = "udm_batch"
KEY_NAME = "gcor-analyts"
KEY_REGION = "us-east1"


def run(cmd):
    result = subprocess.run(
        cmd,
        check=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True,
    )
    return result.stdout.strip()


print("Step 1: Querying Keymaker metadata from Spanner...")

sql = f"""
SELECT
  ks.key_region,
  ki.cloud_kek_identifier,
  ki.cloud_keyring_name,
  TO_BASE64(km.key_material_json_bytes)
FROM key_material_info km
JOIN keyspec_info ks
  ON km.keyspec_info_id = ks.keyspec_info_id
JOIN kek_info ki
  ON km.kek_info_id = ki.kek_info_id
WHERE ks.safename = '{SAFE_NAME}'
  AND ks.keyname = '{KEY_NAME}'
  AND ks.key_region = '{KEY_REGION}'
  AND km.is_active = TRUE
LIMIT 1
"""

output = run([
    "gcloud", "spanner", "databases", "execute-sql",
    DATABASE,
    "--instance", INSTANCE,
    "--project", PROJECT,
    "--format", "json",
    "--sql", sql
])

rows = json.loads(output)

if not rows:
    raise RuntimeError("No active KeySpec metadata found")

row = rows[0]

properties_b64 = row["TO_BASE64(km.key_material_json_bytes)"]

print("  Key region:", row["key_region"])
print("  KMS key:", row["cloud_kek_identifier"])
print("  Key material found: YES")


print("Step 2: Decoding key material properties...")

properties = base64.b64decode(properties_b64).decode("utf-8")

props = {}

for line in properties.splitlines():
    if not line.strip():
        continue

    key, value = line.split("=", 1)
    props[key] = value

gcp_wrapped_dek_b64 = props["gcpWrappedDEK"]
gcp_kms_key = props["gcpKmsKey"]

print("  gcpWrappedDEK found: YES")
print("  gcpKmsKey found: YES")


print("Step 3: Calling Cloud KMS to decrypt wrapped DEK...")

wrapped_dek = base64.b64decode(gcp_wrapped_dek_b64)

with tempfile.NamedTemporaryFile(delete=False) as encrypted_file:
    encrypted_file.write(wrapped_dek)
    encrypted_path = encrypted_file.name

decrypted_path = encrypted_path + ".plain"

try:
    run([
        "gcloud", "kms", "decrypt",
        "--project", PROJECT,
        "--location", KEY_REGION,
        "--keyring", gcp_kms_key.split("/keyRings/")[1].split("/cryptoKeys/")[0],
        "--key", gcp_kms_key.split("/cryptoKeys/")[1],
        "--ciphertext-file", encrypted_path,
        "--plaintext-file", decrypted_path,
    ])

    with open(decrypted_path, "rb") as f:
        decrypted_dek = f.read()

    print("  KMS decrypt: SUCCESS")
    print("  Decrypted DEK length:", len(decrypted_dek), "bytes")

    if len(decrypted_dek) != 32:
        raise RuntimeError(
            f"Unexpected DEK length: {len(decrypted_dek)} bytes"
        )

    print("Step 4: Keymaker fetch simulation: SUCCESS")
    print("  Plaintext DEK was NOT printed.")

finally:
    for path in [encrypted_path, decrypted_path]:
        try:
            os.remove(path)
        except FileNotFoundError:
            pass
