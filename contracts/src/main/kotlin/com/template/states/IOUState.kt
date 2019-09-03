package com.template.states

import com.template.contracts.IOUContract
import net.corda.core.contracts.*
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import java.util.*

// *********
// * State *
// *********
@BelongsToContract(IOUContract::class)
data class IOUState(val amount : Amount<Currency>,
                    val lender : Party,
                    val borrower : Party) : ContractState {

        override val participants get() = listOf(lender , borrower)
}
