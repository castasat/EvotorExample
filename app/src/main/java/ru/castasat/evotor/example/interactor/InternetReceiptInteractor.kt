package ru.castasat.evotor.example.interactor

import android.app.Activity
import android.util.Log
import android.widget.Toast
import ru.evotor.framework.calculator.MoneyCalculator
import ru.evotor.framework.component.PaymentPerformer
import ru.evotor.framework.core.IntegrationException
import ru.evotor.framework.core.IntegrationManagerCallback
import ru.evotor.framework.core.IntegrationManagerFuture
import ru.evotor.framework.core.IntegrationManagerFuture.Result
import ru.evotor.framework.core.action.command.print_receipt_command.PrintBuyReceiptCommand
import ru.evotor.framework.payment.PaymentSystem
import ru.evotor.framework.payment.PaymentType
import ru.evotor.framework.receipt.Payment
import ru.evotor.framework.receipt.Position
import ru.evotor.framework.receipt.Position.Builder
import ru.evotor.framework.receipt.PrintGroup
import ru.evotor.framework.receipt.PrintGroup.Type
import ru.evotor.framework.receipt.Receipt.PrintReceipt
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.CountDownLatch
import kotlin.math.abs

class InternetReceiptInteractor {
    fun execute(activity: Activity) {
        object : Thread() {
            override fun run() {
                super.run()
                val buffer = StringBuffer()
                val random = Random()
                val currentMarkOffset = START_MARK_OFFSET
                for (receiptIndex in 0 until RECEIPT_COUNT) { //12

                    val positionCount =
                        random.nextInt(MAX_POSITION_IN_RECEIPT) + 1

                    val positions: MutableList<Position> =
                        ArrayList()
                    for (i in 0 until positionCount) {
                        val mark: String? = null
                        val quantity =
                            BigDecimal(random.nextInt(6) + 1)
//                        if (random.nextInt(100) < 80 && currentMarkOffset < STOP_MARK_OFFSET) {
//                            mark = getMark(currentMarkOffset);
//                            currentMarkOffset++;
//                            quantity = BigDecimal.ONE;
//                        }


                        positions.add(
                            createPosition(
                                BigDecimal(random.nextInt(200)),
                                quantity,
                                mark
                            )
                        )
                    }
                    val payments: MutableList<Payment> =
                        ArrayList()
                    payments.add(createPayment(positions))
                    val email =
                        "random" + abs(random.nextInt()).toString() + "@someemail.ru"
                    val latch =
                        CountDownLatch(1)
                    logReceipt(buffer, positions, payments, email)
                    try {
                        sleep(100)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                    val paymentsMap: MutableMap<Payment, BigDecimal> =
                        HashMap()
                    val changesMap: MutableMap<Payment, BigDecimal> =
                        HashMap()
                    for (payment in payments) {
                        paymentsMap[payment] = payment.value
                        changesMap[payment] = BigDecimal.ZERO
                    }
                    val receipts: MutableList<PrintReceipt> =
                        ArrayList()
                    receipts.add(
                        PrintReceipt(
                            PrintGroup(
                                UUID.randomUUID().toString(),
                                Type.CASH_RECEIPT,
                                null,
                                null,
                                null,
                                null,
                                true,
                                null
                            ),
                            positions,
                            paymentsMap,
                            changesMap,
                            HashMap()
                        )
                    )
                    PrintBuyReceiptCommand(
//                            positions, payments, null, email
                        receipts, null, null, email, BigDecimal.ZERO
                    ).process(
                        activity,
                        IntegrationManagerCallback { integrationManagerFuture: IntegrationManagerFuture ->
                            try {
                                val result: Result =
                                    integrationManagerFuture.result
                                if (result.type == Result.Type.OK) {
                                    buffer.append("Receipt OK")
                                    buffer.append("\n")
                                } else {
                                    throw RuntimeException("" + result.type.name + " " + result.error.message)
                                }
                            } catch (e: IntegrationException) {
                                e.printStackTrace()
                            } finally {
                                latch.countDown()
                            }
                        }
                    )
                    try {
                        latch.await()
                        activity.runOnUiThread {
                            Toast.makeText(
                                activity,
                                "Пробит чек: $receiptIndex",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e("TAGSSSS", "Пробит чек: $receiptIndex")
                        }
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
                buffer.append("currentMarkOffset ").append(currentMarkOffset).append("\n")
                Log.e(
                    TAG,
                    "currentMarkOffset $currentMarkOffset"
                )
                try {
                    sleep(100)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                activity.runOnUiThread {
                    Log.e(TAG, buffer.toString())
                    Toast.makeText(
                        activity,
                        "Закончили! Раз! Два!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }.start()
    }

    private fun logReceipt(
        buffer: StringBuffer,
        positions: List<Position>,
        payments: List<Payment>,
        email: String
    ) {
        for (position in positions) {
            buffer.append(position.toString())
                .append("\n")
                .append("Position total ")
                .append(position.totalWithoutDocumentDiscount.toPlainString())
                .append("\n")
        }
        for (payment in payments) {
            buffer.append(payment.toString())
            buffer.append("\n")
        }
        buffer.append("Email: ").append(email).append("\n")
    }

    private fun getMark(currentMarkOffset: Int): String {
        val markCode = markCodes[currentMarkOffset]
        return markCode + "AAFF" + "CAFE"
    }

    private fun createPayment(positions: List<Position>): Payment {
        var sum: BigDecimal? = BigDecimal.ZERO
        for (position in positions) {
            sum = MoneyCalculator.add(
                sum,
                position.totalWithoutDocumentDiscount
            )
        }
        val paymentSystem =
            PaymentSystem(
                PaymentType.CASH, "Cash",
                "ru.example.evotor.cash"
            )
        return Payment(
            UUID.randomUUID().toString(),
            sum!!,
            paymentSystem,
            PaymentPerformer(
                paymentSystem,
                "ru.example.evotor.cash",
                "ru.example.evotor.cash",
                UUID.randomUUID().toString(),
                "Cash"
            ),
            "purposeID",
            "accountID",
            "accountUserDescr"
        )
    }

    private fun createPosition(
        price: BigDecimal,
        quantity: BigDecimal,
        mark: String?
    ): Position {
        val random = Random()
        var name = PRODUCT_NAME + random.nextInt(500)
        if (mark != null) {
            name = "Табак. $name"
        }
        val builder: Builder =
            Builder.newInstance(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                name, MEASURE_NAME,
                0,
                price,
                quantity
            )
        if (mark != null) {
            builder.toTobaccoMarked(mark)
        }
        return builder.build()
    }

    companion object {
        private const val TAG = "InternetReceiptInter"
        private const val PRODUCT_NAME = "Лучший товар на этой кассе. Поставка "
        private const val MEASURE_NAME = "шт"
        private val markCodes: List<String?> =
            object : ArrayList<String?>() {
                init {
                    add("00000046203946twkjoIm")
                }
            }
        private const val RECEIPT_COUNT = 12
        private const val MAX_POSITION_IN_RECEIPT = 3
        private const val START_MARK_OFFSET = 312
        private const val STOP_MARK_OFFSET = 500
    }
}