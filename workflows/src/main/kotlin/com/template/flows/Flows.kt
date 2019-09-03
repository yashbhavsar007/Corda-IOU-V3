package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.IOUContract
import com.template.states.IOUState
import net.corda.core.contracts.Amount
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker
import net.corda.core.contracts.Command
import net.corda.core.contracts.Issued
import net.corda.core.contracts.requireThat
import net.corda.core.identity.Party
import net.corda.core.transactions.TransactionBuilder
import java.util.*
import kotlin.math.sign

// *********
// * Flows *
// *********
@InitiatingFlow
@StartableByRPC
class IOUIssueFlow(val amount : Amount<Currency>,
                   val BParty : Party) : FlowLogic<SignedTransaction>(){


    override val progressTracker = ProgressTracker()

    @Suspendable
    override  fun call() : SignedTransaction {
        val notary = serviceHub.networkMapCache.notaryIdentities[0]
        val opState = IOUState(amount,ourIdentity,BParty)
        val issuedCommand = Command(IOUContract.Commands.Issue(),listOf(ourIdentity.owningKey,BParty.owningKey))

        val txBuilder = TransactionBuilder(notary = notary)
                .addOutputState(opState,IOUContract.id)
                .addCommand(issuedCommand)

        txBuilder.verify(serviceHub)
        val signedTx = serviceHub.signInitialTransaction(txBuilder)


        val flowsession = initiateFlow(BParty)
        val allSignedTransaction = subFlow(CollectSignaturesFlow(signedTx , listOf(flowsession),CollectSignaturesFlow.tracker()))
        subFlow(FinalityFlow(allSignedTransaction,flowsession))


        return allSignedTransaction
    }
}

//@InitiatingFlow
//@StartableByRPC
//class IOUTransferFlow(private  val state : IOUState ) : FlowLogic<SignedTransaction>(){
//    override fun call(): SignedTransaction {
//        val notary = serviceHub.networkMapCache.notaryIdentities.first()
//        val TransferredCommand = Command(IOUContract.Commands.Transfer() , state.participants.map { it.owningKey })
//        val transaction = TransactionBuilder( notary = notary)
//        transaction.addOutputState(state , IOUContract.IOU_Contract_ID)
//        transaction.addCommand(TransferredCommand)
//        transaction.verify(serviceHub)
//        val SingleSignedTransaction = serviceHub.signInitialTransaction(transaction)
//        val session = (state.participants - ourIdentity).map { initiateFlow(it) }.toSet()
//        val allSignedTransaction = subFlow(CollectSignaturesFlow(SingleSignedTransaction , session))
//        subFlow(FinalityFlow(allSignedTransaction,session))
//        return  allSignedTransaction
//    }
//}

//@InitiatedBy(IOUFlow::class)
//class IOUFlowResponder(private val otherPartySession: FlowSession) : FlowLogic<Unit>() {
//    @Suspendable
//    override fun call() {
//        val signTransactionFlow = object :  SignTransactionFlow(otherPartySession){
//            // checkTransaction method is already there in SignTransactionFlow but we need to modify as per our requirement
//            override fun checkTransaction(stx: SignedTransaction) = requireThat{
//                val output = stx.tx.outputs.single().data
//                "This must be a iou transaction" using (output is IOUState)
//
//                val iou = output as IOUState
//                "value must be less than 100" using (iou.amount < 100)
//
//            }
//        }
//
//        val expectedTxId = subFlow(signTransactionFlow).id
//
//        subFlow(ReceiveFinalityFlow(otherPartySession , expectedTxId))
//    }
//}

@InitiatedBy(IOUIssueFlow::class)
class IOUIssueFlowResponder( private val flowsession : FlowSession) : FlowLogic<Unit>() {

    @Suspendable
    override fun call() {
        val signedTransactionFlow = object : SignTransactionFlow(flowsession){
            override fun checkTransaction(stx: SignedTransaction) =
                requireThat {
                    val output = stx.tx.outputs.single().data
                    "This must be an IOU transaction " using (output is IOUState)
                }

        }
        val expectedTxId = subFlow(signedTransactionFlow).id
         subFlow(ReceiveFinalityFlow(flowsession , expectedTxId))

    }
}


//@InitiatedBy(IOUTransferFlow :: class)
//class IOUTransferResponder( private val flowSession: FlowSession) : FlowLogic <Unit> (){
//
//    @Suspendable
//    override fun call() {
//        val signedTransactionFlow = object : SignTransactionFlow(flowSession){
//            override fun checkTransaction(stx: SignedTransaction) {
//                requireThat {
//                    val output = stx.tx.outputs.single().data
//                    " This must be an IOU state transaction " using (output is IOUState)
//                }
//            }
//        }
//        val expectedTxId = subFlow(signedTransactionFlow).id
//        subFlow(ReceiveFinalityFlow(flowSession,expectedTxId))
//        //subFlow(signedTransactionFlow)
//    }
//
//}


