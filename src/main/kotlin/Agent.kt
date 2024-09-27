import id.walt.credentials.CredentialBuilder
import id.walt.credentials.CredentialBuilderType
import id.walt.credentials.verification.Verifier
import id.walt.credentials.verification.models.PolicyRequest
import id.walt.credentials.verification.policies.ExpirationDatePolicy
import id.walt.credentials.verification.policies.JwtSignaturePolicy
import id.walt.credentials.verification.policies.NotBeforeDatePolicy
import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.jwk.JWKKey
import id.walt.crypto.utils.JsonUtils.toJsonObject
import id.walt.did.dids.DidService
import id.walt.did.dids.registrar.DidResult
import id.walt.did.dids.registrar.dids.DidJwkCreateOptions
import java.util.*
import kotlin.time.Duration.Companion.days

class Agent (){
    // Agent provides all capabilities that participants in this credential example need.
    // This includes functions to act as a verifier, issuer, and holder

    // this is not a secure way to store this key, but it should be sufficient for this purpose
    private lateinit var jwkKey: JWKKey
    private lateinit var didResult: DidResult

    suspend fun init() {
        // This function creates a jwk key and uses it to create a jwk DID.

        jwkKey = JWKKey.generate(KeyType.Ed25519)

        // The JWK DID method is used for simplicity. With this, no ledgers need to be accessed.
        didResult = DidService.registerByKey("jwk", jwkKey, DidJwkCreateOptions())
        println("DID created: ${didResult.did}")
    }

    fun getDidResult(): DidResult {
        return didResult
    }

    suspend fun issueExampleCredential(subjectDID: String): String {
        // This builds an example W3Cv2 VCs and uses the agent's jwk key to sign it.
        // Returns the signed VC

        val credentialBuilder = CredentialBuilder(CredentialBuilderType.W3CV2CredentialBuilder)
        val vc = credentialBuilder.apply{
            addContext("https://purl.imsglobal.org/spec/ob/v3p0/context-3.0.3.json")
            addType("OpenBadgeCredential")
            credentialId = UUID.randomUUID().toString()

            //set this agent as issuer
            issuerDid = didResult.did

            // set validity period
            validFromNow()
            validFor(10.days)

            subjectDid = subjectDID

            // add credential subject data
            useCredentialSubject(
                mapOf(
                    "type" to listOf("AchievementSubject"),
                    "achievement" to mapOf(
                        "id" to UUID.randomUUID().toString(),
                        "type" to listOf("Achievement"),
                        "criteria" to mapOf(
                            "type" to "Criteria",
                            "narrative" to "Example narrative."
                        ),
                        "name" to "Coding Challenge Example",
                        "description" to "This is an example VC",
                    )
                ).toJsonObject()
            )
        }.buildW3C()
        println("Verifiable Credential created:")
        println(vc.toPrettyJson())
        return vc.signJws(jwkKey, didResult.did, subjectDid = subjectDID)
    }

    suspend fun verifyVC(vc: String): Boolean {
        // This function takes as input a VC and returns true if the signature is correct and the credential is within
        // its validity period. Otherwise, this function returns false.

        val results = Verifier.verifyCredential(
            vc,
            listOf(
                PolicyRequest(JwtSignaturePolicy()),
                PolicyRequest(ExpirationDatePolicy()),
                PolicyRequest(NotBeforeDatePolicy()),
            )
        )
        return results.all { it.isSuccess() }
    }
}
