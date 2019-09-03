package com.template.contracts

import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction
import com.template.states.IOUState


// ************
// * Contract *
// ************
class IOUContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        val id = "com.template.contracts.IOUContract"
    }

    interface Commands : CommandData{

        class Issue : TypeOnlyCommandData(), Commands
        class Transfer : TypeOnlyCommandData(), Commands
        class Settle : TypeOnlyCommandData(), Commands
    }
    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    override fun verify(tx: LedgerTransaction) {
        // Verification logic goes here.

        val iou = tx.outputStates.single() as IOUState
        val command = tx.commands.requireSingleCommand<Commands>()
        when (command.value){
            is Commands.Issue -> requireThat {
                " Required that inputs should not be emptied " using (tx.inputs.isEmpty())
                " Required that only one output state should be created while issuing transaction " using (tx.outputs.size == 1)
                " Amount must be positive value " using (iou.amount.quantity > 0)
                " Borrower and Lender cannot be same party " using (iou.borrower != iou.lender)
                " Both borrower and lender together may sign the transaction " using
                        ( command.signers.toSet().size == 2 )

            }

            is Commands.Transfer -> requireThat {
               val input = tx.inputsOfType<IOUState>().single()

                " Requires that amount must be positive " using (iou.amount.quantity > 0)
                " You don't have enough balance to transfer " using (iou.amount.quantity >  input.amount.quantity)
                " Lender and Borrower must not be same " using (iou.lender != iou.borrower)
            }

            is Commands.Settle -> requireThat {

            }


        }
    }

    // Used to indicate the transaction's intent.

}