import id.walt.did.dids.DidService


suspend fun main() {
    DidService.minimalInit()

    // Init issuer, holder and verifier for this example
    println("Creating the issuer")
    val issuer = Agent()
    issuer.init()

    println("Creating the holder")
    val holder = Agent()
    holder.init()

    println("Creating the verifier")
    val verifier = Agent()
    verifier.init()

    // Issue credential
    val vc = issuer.issueExampleCredential(holder.getDidResult().did)

    // The issued credential should be stored in the Holder wallet.
    // For simplicity, this is omitted here.

    // verify the VC
    // in a real application, the holder would have to create and sign a VP for the verifier
    val verified = verifier.verifyVC(vc)
    println("Successful verification of the VC? $verified")
}