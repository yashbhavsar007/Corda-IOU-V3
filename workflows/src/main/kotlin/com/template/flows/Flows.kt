package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.IOUContract
import com.template.states.IOUState
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker
import net.corda.core.contracts.Command
// *********
// * Flows *
// *********
@InitiatingFlow
@StartableByRPC
class IOUIssueFlow( private  val state : IOUState) : FlowLogic<SignedTransaction>(){

    override  fun call() : SignedTransaction {
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val issuedCommand = Command()
    }
}
