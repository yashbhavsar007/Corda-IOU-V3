package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.IOUContract
import com.template.states.IOUState
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker
import net.corda.core.contracts.Command
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.TransactionBuilder

// *********
// * Flows *
// *********
@InitiatingFlow
@StartableByRPC
class IOUIssueFlow( private  val state : IOUState) : FlowLogic<SignedTransaction>(){

    override  fun call() : SignedTransaction {
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val issuedCommand = Command(IOUContract.Commands.Issue(),state.participants.map { it.owningKey })
        val transaction = TransactionBuilder(notary = notary)
        transaction.addOutputState(state,IOUContract.IOU_Contract_ID)
        transaction.addCommand(issuedCommand)
        transaction.verify(serviceHub)
        val SingleSignedTransaction = serviceHub.signInitialTransaction(transaction)
        val session = (state.participants - ourIdentity).map { initiateFlow(it) }.toSet()
        val allSignedTransaction = subFlow(CollectSignaturesFlow(SingleSignedTransaction , session))
        subFlow(FinalityFlow(allSignedTransaction))
        return allSignedTransaction
    }
}

class IOUTransferFlow(private  val state : IOUState ) : FlowLogic<SignedTransaction>(){

}





@InitiatedBy(IOUIssueFlow::class)
class IOUIssueFlowResponder( private val flowSession : FlowSession) : FlowLogic<Unit>(){

    @Suspendable
    override fun call() {
        val signedTransactionFlow = object : SignTransactionFlow(flowSession){
            override fun checkTransaction(stx: SignedTransaction) {
                requireThat {
                    val output = stx.tx.outputs.single().data
                    "This must be an IOU transaction " using (output is IOUState)
                }
            }
        }
        subFlow(signedTransactionFlow)
    }
}


