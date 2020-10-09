package com.mycelium.wallet.activity.fio.requests.viewmodels

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mycelium.wallet.activity.fio.requests.ApproveFioRequestSuccessActivity
import com.mycelium.wallet.activity.receive.ReceiveCoinsActivity
import com.mycelium.wallet.activity.send.ManualAddressEntry
import com.mycelium.wallet.activity.send.model.SendCoinsViewModel
import com.mycelium.wallet.activity.send.model.SendFioModel
import com.mycelium.wallet.activity.util.BtcFeeFormatter
import com.mycelium.wallet.activity.util.FeeFormatter
import com.mycelium.wapi.wallet.Address
import com.mycelium.wapi.wallet.WalletAccount
import com.mycelium.wapi.wallet.coins.Value
import com.mycelium.wapi.wallet.fio.FioModule
import com.mycelium.wapi.wallet.fio.RegisteredFIOName
import com.mycelium.wapi.wallet.fio.getFioAccounts
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import java.util.regex.Pattern

class FioRequestCreateViewModel(val app: Application) : SendCoinsViewModel(app) {
    override val uriPattern = Pattern.compile("[a-zA-Z0-9]+")!!
    override fun sendTransaction(activity: Activity) {
        TODO("Not yet implemented")
    }

    override fun getFeeFormatter(): FeeFormatter = BtcFeeFormatter()

    private var fioModule: FioModule

    override fun init(account: WalletAccount<*>, intent: Intent) {
        super.init(account, intent)
        val fioAccounts = mbwManager.getWalletManager(false).getFioAccounts()
        if (fioAccounts.isNotEmpty()) {
            val fioAccount = fioAccounts[0]
            model = SendFioModel(app, fioAccount, intent)
        }
        val account = mbwManager.selectedAccount
        val fioNames = fioModule.getFIONames(account)
        if (fioNames.isEmpty()) {
            TODO("Handle case when account to registered")
        }

        payeeAccount.value = account
        payeeFioAddreses.value = fioNames
        payeeFioName.value = fioNames[0].name
        payeeTokenPublicAddress.value = account.receiveAddress.toString()
    }

    val payerTokenPublicAddress = MutableLiveData<String>()
    val payeeTokenPublicAddress = MutableLiveData<String>()
    val payeeFioAddreses = MutableLiveData<List<RegisteredFIOName>>()
    val payeeAccount = MutableLiveData<WalletAccount<*>>()

    init {
        val walletManager = mbwManager.getWalletManager(false)
        fioModule = walletManager.getModuleById(FioModule.ID) as FioModule
    }


    fun sendRequest(activity: Activity, doOnSuccess: (Unit) -> Unit, doOnError: (Exception) -> Unit) {
        viewModelScope.launch(IO) {
            val fioAccounts = mbwManager.getWalletManager(false).getFioAccounts()
            if (!fioAccounts.isEmpty()) {
                val fioAccount = fioAccounts[0]
                val transferTokensFee = fioAccount.getTransferTokensFee()
                val selectedAccount = mbwManager.selectedAccount
                try {
                    val requestFunds = fioAccount.requestFunds(
                            payerFioName.value!!,
                            payeeFioName.value!!,
                            payeeTokenPublicAddress.value!!,
                            getAmount().value?.value?.toDouble()!!,
                            selectedAccount.basedOnCoinType.symbol,
                            selectedAccount.coinType.symbol,
                            transferTokensFee)

                    withContext(Main) {
                        ApproveFioRequestSuccessActivity.start(
                                activity,
                                getAmount().value ?: Value.zeroValue(selectedAccount.coinType),
                                getFiatValue() ?: "",
                                getSelectedFee().value ?: Value.zeroValue(selectedAccount.coinType),
                                Date().time,
                                payerFioName.value!!,
                                payeeFioName.value!!,
                                fioMemo.value ?: "",
                                byteArrayOf(),
                                accountId = selectedAccount.id)
                    }
                } catch (ex: Exception) {
                    withContext(Main) {
                        doOnError.invoke(ex)
                    }
                }

            }
        }
    }


    fun getPayeeFioAddreses(): List<RegisteredFIOName>? {
        return payeeFioAddreses.value
    }

    override fun processReceivedResults(requestCode: Int, resultCode: Int, data: Intent?, activity: Activity) {
        super.processReceivedResults(requestCode, resultCode, data, activity)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ReceiveCoinsActivity.MANUAL_ENTRY_RESULT_CODE && !data?.getStringExtra(ManualAddressEntry.ADDRESS_RESULT_FIO).isNullOrBlank()) {
                val fioAddress = data?.getStringExtra(ManualAddressEntry.ADDRESS_RESULT_FIO)!!
                val addressResult = data?.getSerializableExtra(ManualAddressEntry.ADDRESS_RESULT_NAME) as Address
                payerFioName.value = fioAddress
                payerTokenPublicAddress.value = addressResult.toString()
            }
        }
    }
}